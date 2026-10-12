package com.grtc.main.global.file;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

// 게시판 첨부파일을 디스크에 저장·조회·삭제하는 공용 도우미 (자료실에서 사용)
// 규칙은 민원 첨부파일과 같다: jpg, jpeg, png, gif, pdf / 파일당 10MB / 글당 최대 5개
//
// 트랜잭션 안에서 호출해야 한다.
//   - store: 트랜잭션이 롤백되면 방금 쓴 파일을 지운다 (DB 에 없는 파일이 남지 않게)
//   - deleteAfterCommit: 커밋이 끝난 뒤에 지운다 (롤백되면 파일이 그대로 남는다)
@Slf4j
@Component
public class FileStorage {

    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "pdf");
    public static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    public static final int MAX_FILE_COUNT = 5;
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM");

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    private Path root;

    // 디스크에 저장된 파일 정보. relativeDir 는 업로드 폴더 기준 경로 (예: archive/2026/10)
    public record StoredFile(String originalName, String savedName, String relativeDir,
                             long size, String contentType) {
    }

    @PostConstruct
    void init() {
        root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 폴더를 만들 수 없습니다: " + root, e);
        }
    }

    // 비어 있는 파트를 뺀 실제 파일 목록
    public List<MultipartFile> nonEmpty(List<MultipartFile> files) {
        return files == null ? List.of() : files.stream().filter(f -> f != null && !f.isEmpty()).toList();
    }

    // 개수·크기·확장자 검사. 하나라도 틀리면 아무것도 저장하지 않도록 저장 전에 먼저 부른다
    public void validate(List<MultipartFile> files, int existingCount) {
        if (existingCount + files.size() > MAX_FILE_COUNT) {
            throw new BusinessException(ErrorCode.FILE_TOO_MANY);
        }
        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
            }
            if (!ALLOWED_EXTENSIONS.contains(extensionOf(cleanFileName(file)))) {
                throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
            }
        }
    }

    // category 폴더(예: "archive") 아래 연/월 폴더에 UUID 이름으로 저장
    public StoredFile store(String category, MultipartFile file) {
        String originalName = cleanFileName(file);
        String savedName = UUID.randomUUID() + "." + extensionOf(originalName);
        String relativeDir = category + "/" + LocalDate.now().format(DATE_DIR);

        Path dir = root.resolve(relativeDir).normalize();
        Path target = dir.resolve(savedName).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(ErrorCode.FILE_INVALID_PATH);
        }
        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            deleteQuietly(target);
            log.error("[file] 저장 실패 name={}", originalName, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        deleteOnRollback(target);
        return new StoredFile(originalName, savedName, relativeDir, file.getSize(), file.getContentType());
    }

    public Resource load(String relativeDir, String savedName) {
        Path file = resolve(relativeDir, savedName);
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    public void deleteAfterCommit(String relativeDir, String savedName) {
        Path file = resolve(relativeDir, savedName);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteQuietly(file);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(file);
            }
        });
    }

    private void deleteOnRollback(Path file) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    deleteQuietly(file);
                }
            }
        });
    }

    private Path resolve(String relativeDir, String savedName) {
        Path file = root.resolve(relativeDir).resolve(savedName).normalize();
        if (!file.startsWith(root)) {
            throw new BusinessException(ErrorCode.FILE_INVALID_PATH);
        }
        return file;
    }

    private String cleanFileName(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.FILE_INVALID_NAME);
        }
        String cleaned = StringUtils.cleanPath(name);
        cleaned = cleaned.substring(cleaned.lastIndexOf('/') + 1);
        if (cleaned.isBlank() || cleaned.length() > 255) {
            throw new BusinessException(ErrorCode.FILE_INVALID_NAME);
        }
        return cleaned;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("[file] 파일 삭제 실패 path={}", path, e);
        }
    }
}
