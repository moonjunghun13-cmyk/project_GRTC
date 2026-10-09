package com.grtc.main.member;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

// 프로필 이미지 파일 조회 (권한: 전체)
//   회원 정보 응답의 profileImageUrl / profileThumbnailUrl 주소가 여기로 온다.
//   <img src> 로 바로 쓰기 위해 JSON 이 아니라 이미지 파일 그대로 내려준다.
@RestController
@RequestMapping("/api/v1/files/profile")
@RequiredArgsConstructor
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> get(@PathVariable String fileName) {
        Resource resource = new FileSystemResource(profileImageService.find(fileName));
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                // 이미지를 바꾸면 파일명이 달라지므로 오래 캐시해도 된다.
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(resource);
    }
}
