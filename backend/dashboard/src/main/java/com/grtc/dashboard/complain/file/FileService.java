package com.grtc.dashboard.complain.file;

import com.grtc.dashboard.global.exception.BusinessException;
import com.grtc.dashboard.global.exception.ErrorCode;
import com.grtc.dashboard.complain.ComplainEntity;
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

// 민원 첨부파일 저장 / 조회 / 다운로드 읽기 / 삭제
//  - 파일 내용은 서버 디스크(app.file.upload-dir)에, 정보는 DB(Attachment)에 저장한다.
//  - "누가 내려받을 수 있는가"는 이 클래스가 아니라 ComplainService 에서 확인한다.
@Slf4j
@Service
@RequiredArgsConstructor        // 생성자 주입 (final 필드만 생성자에 포함된다)
@Transactional(readOnly = true) // 기본 읽기 전용, 쓰기 메서드에만 @Transactional
public class FileService {

    // 허용하는 확장자 (화이트리스트) - 민원 양식 안내문: "이미지 또는 PDF 파일을 첨부할 수 있습니다."
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "pdf");

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;   // 파일 1개 최대 10MB
    public static final int MAX_FILE_COUNT = 5;                    // 민원 1건당 최대 5개
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FileRepository fileRepository;

    // 저장 폴더는 코드에 쓰지 않고 설정값(application.yaml)에서 읽는다.
    @Value("${app.file.upload-dir}")
    private String uploadDir;

    // 업로드 폴더의 절대 경로 (서비스 시작 시 한 번 계산)
    private Path root;

    // 서비스가 시작될 때 업로드 폴더를 계산하고, 없으면 만든다.
    @PostConstruct
    void init() {
        root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 폴더를 만들 수 없습니다: " + root, e);
        }
    }

    // =========================================================
    // 1) 저장
    // =========================================================
    @Transactional // DB 에 저장하므로 readOnly 해제
    public List<Attachment> store(ComplainEntity complain, List<MultipartFile> files) {

        if (files == null) {
            return List.of();
        }

        // 파일을 선택하지 않아도 "빈 파일 1개"가 넘어오는 경우가 있어서 걸러낸다.
        List<MultipartFile> targets = files.stream()
                .filter(file -> !file.isEmpty())
                .toList();

        if (targets.isEmpty()) {
            return List.of();
        }

        // 파일 개수 제한
        if (targets.size() > MAX_FILE_COUNT) {
            throw new BusinessException(ErrorCode.FILE_TOO_MANY);
        }

        // 하나라도 문제가 있으면 하나도 저장하지 않도록, 저장 전에 전부 먼저 검증한다.
        targets.forEach(this::validate);

        List<Path> written = new ArrayList<>();          // 디스크에 쓴 파일 (실패 시 지우기 위해 기록)
        List<Attachment> saved = new ArrayList<>();

        try {
            for (MultipartFile file : targets) {
                String originalName = cleanFileName(file);
                String extension = extensionOf(originalName);

                // 저장 이름은 UUID 로 새로 만든다. (덮어쓰기, 특수문자/경로 조작 방지)
                String savedName = UUID.randomUUID() + "." + extension;

                // 연/월 폴더로 나눠서 저장하고, DB 에는 상대 경로만 보관한다.
                String relativeDir = LocalDate.now().format(DATE_DIR);   // 예: 2026/10
                Path dir = root.resolve(relativeDir).normalize();
                Path target = dir.resolve(savedName).normalize();

                // 저장 위치가 업로드 폴더 "안"인지 확인 (경로 조작 공격 방지)
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
            // 디스크 오류 - 이미 쓴 파일을 지워서 "DB 에는 없는데 디스크에만 남은 파일"을 막는다.
            deleteAllQuietly(written);
            log.error("[store] 파일 저장 실패 complainId={}", complain.getId(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);

        } catch (RuntimeException e) {
            // DB 저장 실패 등 - 파일을 정리하고 원래 예외를 그대로 다시 던진다.
            deleteAllQuietly(written);
            throw e;
        }
    }

    // =========================================================
    // 1-1) 검증 (저장 전에 모든 파일에 대해 실행)
    // =========================================================
    private void validate(MultipartFile file) {

        // 크기 확인 (설정값과 별개로 서비스에서도 한 번 더)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        // 확장자는 허용 목록으로 검사한다. (확장자가 없으면 빈 문자열이라 걸러진다)
        String extension = extensionOf(cleanFileName(file));
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }

        // file.getContentType() 은 사용자가 속일 수 있어서 판단 기준으로 쓰지 않는다.
    }

    // =========================================================
    // 2) 조회
    // =========================================================
    public Attachment getAttachment(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }

    public List<Attachment> findByComplain(Long complainId) {
        return fileRepository.findAllByComplainId(complainId);
    }

    // =========================================================
    // 3) 다운로드용 파일 읽기
    //    ※ "누가 내려받을 수 있는가"(민원인 본인, 관리자)는
    //      이 메서드를 부르기 전에 ComplainService 에서 반드시 확인한다.
    // =========================================================
    public Resource loadAsResource(Attachment attachment) {

        Path file = resolvePath(attachment);

        try {
            Resource resource = new UrlResource(file.toUri());

            // DB 에는 있는데 디스크에서 사라진 경우
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
            }
            return resource;

        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    // =========================================================
    // 4) 삭제 (반드시 트랜잭션 안에서 호출해야 한다)
    // =========================================================
    @Transactional // DB 삭제가 있으므로 readOnly 해제
    public void delete(Long attachmentId) {

        Attachment attachment = getAttachment(attachmentId);
        Path file = resolvePath(attachment);

        fileRepository.delete(attachment);

        // 파일은 DB 삭제가 "커밋(확정)된 뒤"에 지운다.
        // 먼저 지웠는데 DB 삭제가 실패하면 DB 에는 있는데 파일은 없는 상태가 된다.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(file);
            }
        });
    }

    // =========================================================
    // 내부 도우미
    // =========================================================

    // DB 에 저장된 상대 경로 + 저장 이름 -> 실제 경로 (업로드 폴더 밖이면 거부)
    private Path resolvePath(Attachment attachment) {
        Path file = root.resolve(attachment.getFilePath())
                .resolve(attachment.getSavedFileName())
                .normalize();

        if (!file.startsWith(root)) {
            throw new BusinessException(ErrorCode.FILE_INVALID_PATH);
        }
        return file;
    }

    // 사용자가 보낸 원래 이름 정리: null 방지, 경로 정리, 마지막 이름 부분만 사용
    private String cleanFileName(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.FILE_INVALID_NAME);
        }

        // cleanPath: 역슬래시를 슬래시로 바꾸고 "../" 같은 경로를 정리한다.
        String cleaned = StringUtils.cleanPath(name);
        cleaned = cleaned.substring(cleaned.lastIndexOf('/') + 1);

        // DB 컬럼 길이(255)를 넘지 않도록 제한
        if (cleaned.isBlank() || cleaned.length() > 255) {
            throw new BusinessException(ErrorCode.FILE_INVALID_NAME);
        }
        return cleaned;
    }

    // 확장자 추출 (소문자). 확장자가 없으면 빈 문자열.
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

    // 파일 삭제 실패는 사용자 오류로 만들지 않고 로그만 남긴다. (나중에 정리 작업으로 치울 수 있음)
    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("[file] 파일 삭제 실패 path={}", path, e);
        }
    }
}
