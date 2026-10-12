package com.grtc.main.login.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum AdminPage {
    DASHBOARD("대시보드", "/dashboard", "/api/v1/admin/dashboard"),
    VEHICLES("차량관리", "/dashboard/vehicles", "/api/v1/admin/vehicles"),
    DISPATCHES("배차관리", "/dashboard/dispatches", "/api/v1/admin/dispatches"),
    OPERATIONS("운행관리", "/dashboard/operations", "/api/v1/admin/operations"),
    COMPLAINTS("민원관리", "/dashboard/complaints", "/api/admin/complaints"),
    MEMBERS("회원관리", "/dashboard/members", "/api/v1/admin/members"),
    NOTICES("공지관리", "/dashboard/notices", "/api/v1/admin/notices"),
    ARCHIVES("자료실관리", "/dashboard/archives", "/api/v1/admin/archives");

    private static final String COMMON_API = "/api/v1/admin/me";

    private final String label;
    private final String path;
    private final String apiPrefix;

    public static Optional<AdminPage> ofApi(String uri) {
        return Arrays.stream(values())
                .filter(page -> matches(uri, page.apiPrefix))
                .findFirst();
    }

    public static boolean isCommonApi(String uri) {
        return matches(uri, COMMON_API);
    }

    private static boolean matches(String uri, String prefix) {
        return uri != null && (uri.equals(prefix) || uri.startsWith(prefix + "/"));
    }
}
