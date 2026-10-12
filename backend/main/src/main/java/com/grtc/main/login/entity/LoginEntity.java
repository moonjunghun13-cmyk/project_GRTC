package com.grtc.main.login.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키(DB에서 자동 증가)

    @Column(name = "login_id", nullable = false, unique = true, length = 20)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name; // 이름(필수)

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone; // 연락처(선택)

    @Column(length = 50)
    private String department; // 소속 부서(관리자 계정용, 선택)

    @Column(name = "job_position", length = 50)
    private String position; // 직급(관리자 계정용, 선택)

    @Column(name = "profile_image", length = 100)
    private String profileImage;

    @Column(name = "profile_thumbnail", length = 100)
    private String profileThumbnail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_type", length = 30)
    private AdminType adminType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MemberStatus status = MemberStatus.NORMAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 가입일시

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void updateProfile(String name, String email, String phone, String department, String position) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.position = position;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeProfileImage(String profileImage, String profileThumbnail) {
        this.profileImage = profileImage;
        this.profileThumbnail = profileThumbnail;
    }

    public void changeRole(Role role) {
        this.role = role;
        if (role != Role.ADMIN) {
            this.adminType = null;
        }
    }

    public void changeAdminType(AdminType adminType) {
        this.adminType = adminType;
    }

    public AdminType resolveAdminType() {
        if (role != Role.ADMIN) {
            return null;
        }
        return adminType != null ? adminType : AdminType.SYSTEM_ADMIN;
    }

    public void changeStatus(MemberStatus status) {
        this.status = status;
    }
}
