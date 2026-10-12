package com.grtc.main.qna.file;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.qna.complain.ComplainEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "pdf");

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    public static final int MAX_FILE_COUNT = 5;
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FileRepository fileRepository;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    private Path root;

    @PostConstruct
    void init() {
        root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 폴더를 만들 수 없습니다: " + root, e);
        }
    }

    @Transactional
    public List<Attachment> store(ComplainEntity complain, List<MultipartFile> files) {

        if (files == null) {
            return List.of();
        }

        List<MultipartFile> targets = files.stream()
                .filter(file -> !file.isEmpty())
                .toList();

        if (targets.isEmpty()) {
            return List.of();
        }

        if (targets.size() > MAX_FILE_COUNT) {
            throw new BusinessException(ErrorCode.FILE_TOO_MANY);
        }

        targets.forEach(this::validate);

        List<Path> written = new ArrayList<>();
        List<Attachment> saved = new ArrayList<>();

        try {
            for (MultipartFile file : targets) {
                String originalName = cleanFileName(file);
                String extension = extensionOf(originalName);

                String savedName = UUID.randomUUID() + "." + extension;

                String relativeDir = LocalDate.now().format(DATE_DIR);
                Path dir = root.resolve(relativeDir).normalize();
                Path target = dir.resolve(savedName).normalize();

                if (!target.startsWith(root)) {
                    throw new BusinessException(ErrorCode.FILE_INVALID_PATH);
                }

                Files.createDirectories(dir);
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                written.add(target);

                saved.add(fileRepository.save(
                        Attachment.builder()
                                .complain(complain)
                                .originalFileName(originalName)
                                .savedFileName(savedName)
                                .filePath(relativeDir)
                                .fileSize(file.getSize())
                                .contentType(file.getContentType())
                                .build()
                ));
            }
            return saved;

        } catch (IOException e) {
            deleteAllQuietly(written);
            log.error("[store] 파일 저장 실패 complainId={}", complain.getId(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);

        } catch (RuntimeException e) {
            deleteAllQuietly(written);
            throw e;
        }
    }

    private void validate(MultipartFile file) {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        String extension = extensionOf(cleanFileName(file));
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }

    }

    public Attachment getAttachment(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }

    public List<Attachment> findByComplain(Long complainId) {
        return fileRepository.findAllByComplainId(complainId);
    }

    public Resource loadAsResource(Attachment attachment) {

        Path file = resolvePath(attachment);

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

    @Transactional
    public void delete(Long attachmentId) {

        Attachment attachment = getAttachment(attachmentId);
        Path file = resolvePath(attachment);

        fileRepository.delete(attachment);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(file);
            }
        });
    }

    private Path resolvePath(Attachment attachment) {
        Path file = root.resolve(attachment.getFilePath())
                .resolve(attachment.getSavedFileName())
                .normalize();

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

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void deleteAllQuietly(List<Path> paths) {
        paths.forEach(this::deleteQuietly);
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("[file] 파일 삭제 실패 path={}", path, e);
        }
    }
}
