package com.grtc.main.member;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

// 프로필 이미지 저장 / 썸네일 생성 / 조회 (명세서 3-3 ② PUT /members/me/profile-image)
//  - 저장 위치: {app.file.upload-dir}/profile
//  - 원본은 "임의값.확장자", 썸네일은 "임의값_thumb.jpg" (정사각형 160px) 로 저장한다.
//  - 허용 형식: jpg, png, gif / 파일당 10MB 이하 (명세서 3-2 FILE_TOO_LARGE, UNSUPPORTED_FILE_TYPE)
@Slf4j
@Service
public class ProfileImageService {

    // 프로필 이미지를 내려주는 주소 (ProfileImageController)
    public static final String URL_PREFIX = "/api/v1/files/profile/";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;   // 파일당 10MB
    private static final int THUMBNAIL_SIZE = 160;                 // 썸네일 한 변(px)
    // 이 서비스가 만든 파일명만 조회를 허용한다. (경로 조작 방지)
    private static final Pattern STORED_NAME =
            Pattern.compile("^[0-9a-f]{32}(_thumb)?\\.(jpg|jpeg|png|gif)$");

    private final Path directory;

    public ProfileImageService(@Value("${app.file.upload-dir}") String uploadDir) {
        this.directory = Paths.get(uploadDir, "profile").toAbsolutePath().normalize();
    }

    // 저장된 파일명 (원본 / 썸네일)
    public record Stored(String imageName, String thumbnailName) {
    }

    // 저장 파일명 -> 프론트에 내려줄 주소 (없으면 null)
    public static String urlOf(String storedName) {
        return (storedName == null || storedName.isBlank()) ? null : URL_PREFIX + storedName;
    }

    // 업로드한 이미지를 검사한 뒤 원본과 썸네일을 저장한다.
    public Stored store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }

        try {
            byte[] bytes = file.getBytes();
            // 확장자만 바꾼 파일을 걸러내기 위해 실제로 이미지로 읽히는지 확인
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
            }

            Files.createDirectories(directory);
            String id = UUID.randomUUID().toString().replace("-", "");
            String imageName = id + "." + extension;
            String thumbnailName = id + "_thumb.jpg";

            Files.write(directory.resolve(imageName), bytes);
            ImageIO.write(thumbnail(image), "jpg", directory.resolve(thumbnailName).toFile());
            return new Stored(imageName, thumbnailName);
        } catch (IOException e) {
            log.error("[profile] 이미지 저장 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 예전 프로필 이미지 파일 삭제 (실패해도 요청은 계속 진행)
    public void delete(String... storedNames) {
        for (String name : storedNames) {
            if (name == null || !STORED_NAME.matcher(name).matches()) {
                continue;
            }
            try {
                Files.deleteIfExists(directory.resolve(name));
            } catch (IOException e) {
                log.warn("[profile] 예전 이미지 삭제 실패 name={}", name);
            }
        }
    }

    // 조회할 파일 경로 (이 서비스가 만든 파일명이 아니거나 파일이 없으면 404)
    public Path find(String storedName) {
        if (storedName == null || !STORED_NAME.matcher(storedName).matches()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        Path path = directory.resolve(storedName).normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return path;
    }

    // 가운데를 정사각형으로 잘라 160px 썸네일을 만든다. (투명 배경은 흰색으로 채움)
    private BufferedImage thumbnail(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;

        BufferedImage result = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
            graphics.drawImage(source, 0, 0, THUMBNAIL_SIZE, THUMBNAIL_SIZE, x, y, x + side, y + side, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
