package com.grtc.main.login.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "login")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

}
