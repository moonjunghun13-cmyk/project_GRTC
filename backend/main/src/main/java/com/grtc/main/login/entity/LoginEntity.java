package com.grtc.main.login.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// member 테이블과 매핑되는 회원 엔티티
// ※ 예전 login 테이블(nickname/email 기반)과 컬럼 구조가 달라서 새 테이블(member)을 사용한다.
//    (ddl-auto: update 는 기존 테이블의 NOT NULL 컬럼을 지우지 못해 insert 가 실패하기 때문)
// ※ dashboard 서버의 MemberEntity 와 "같은 테이블"을 함께 쓴다. 컬럼을 바꿀 때는 두 클래스를 같이 바꿔야 한다.
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
    private String loginId; // 로그인 아이디(필수, 중복 불가, 소문자로 저장)

    @Column(nullable = false)
    private String password; // 암호화된 비밀번호(필수)

    @Column(nullable = false, length = 30)
    private String name; // 이름(필수)

    @Column(unique = true, length = 100)
    private String email; // 이메일(회원가입 때 필수로 받는다. 중복 불가. 예전에 가입한 회원은 비어 있을 수 있음)

    @Column(length = 20)
    private String phone; // 연락처(선택)

    @Column(length = 50)
    private String department; // 소속 부서(관리자 계정용, 선택)

    @Column(name = "job_position", length = 50)
    private String position; // 직급(관리자 계정용, 선택)

    @Column(name = "profile_image", length = 100)
    private String profileImage; // 프로필 이미지 저장 파일명(선택)

    @Column(name = "profile_thumbnail", length = 100)
    private String profileThumbnail; // 프로필 썸네일 저장 파일명(선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER; // 회원 권한(기본값 USER, 문자열로 저장)

    // 관리자 유형(시스템 관리자 / 부서장 / 민원 담당자 / 차량 담당자). 유형마다 볼 수 있는 관리자 페이지가 다르다.
    //  - 일반회원은 null
    //  - 관리자인데 null 이면 시스템 관리자로 본다. (이 칸이 생기기 전에 만들어진 관리자 계정)
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_type", length = 30)
    private AdminType adminType;

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

    // 비밀번호 변경 (암호화된 값을 넘겨야 한다)
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 프로필 이미지 변경 (원본 / 썸네일 저장 파일명)
    public void changeProfileImage(String profileImage, String profileThumbnail) {
        this.profileImage = profileImage;
        this.profileThumbnail = profileThumbnail;
    }

    // 권한 변경. 일반회원이 되면 관리자 유형은 지운다.
    //  (회원관리에서 관리자로 올린 계정은 유형이 비어 있으므로 시스템 관리자로 취급된다)
    public void changeRole(Role role) {
        this.role = role;
        if (role != Role.ADMIN) {
            this.adminType = null;
        }
    }

    public void changeAdminType(AdminType adminType) {
        this.adminType = adminType;
    }

    // 실제로 적용되는 관리자 유형: 관리자가 아니면 null, 관리자인데 유형이 비어 있으면 시스템 관리자
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
