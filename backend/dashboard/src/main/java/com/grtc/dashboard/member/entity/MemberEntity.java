package com.grtc.dashboard.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// member 테이블과 매핑되는 회원 엔티티 (관리자 서버용)
// ※ main 서버의 LoginEntity 와 "같은 테이블"을 함께 쓴다. 컬럼을 바꿀 때는 두 클래스를 같이 바꿔야 한다.
//    (회원가입/로그인은 main 서버가 담당하고, 이 서버는 회원관리 화면에서 조회·수정만 한다)
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키(DB에서 자동 증가)

    @Column(name = "login_id", nullable = false, unique = true, length = 20)
    private String loginId; // 로그인 아이디(필수, 중복 불가, 소문자로 저장)

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호(필수)

    @Column(nullable = false, length = 30)
    private String name; // 이름(필수)

    @Column(unique = true, length = 100)
    private String email; // 이메일(회원가입 화면에는 없어서 선택, 입력 시 중복 불가)

    @Column(length = 20)
    private String phone; // 연락처(선택)

    @Column(length = 50)
    private String department; // 소속 부서(관리자 계정용, 선택)

    @Column(name = "job_position", length = 50)
    private String position; // 직급(관리자 계정용, 선택)

    @Column(name = "profile_image", length = 100)
    private String profileImage; // 프로필 이미지 저장 파일명(선택, 변경은 main 서버에서 한다)

    @Column(name = "profile_thumbnail", length = 100)
    private String profileThumbnail; // 프로필 썸네일 저장 파일명(선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER; // 회원 권한(기본값 USER, 문자열로 저장)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MemberStatus status = MemberStatus.NORMAL; // 회원 상태(기본값 정상)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 가입일시

    // 저장 직전에 가입일시를 자동으로 채운다.
    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // 회원정보 화면에서 수정하는 항목
    public void updateProfile(String name, String email, String phone, String department, String position) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.position = position;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void changeStatus(MemberStatus status) {
        this.status = status;
    }
}
