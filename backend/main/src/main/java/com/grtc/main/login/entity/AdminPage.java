package com.grtc.main.login.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

// 관리자 화면의 페이지(메뉴) 목록
//   label     : 화면에 보여줄 메뉴 이름
//   path      : 프론트 화면 주소
//   apiPrefix : 그 페이지가 쓰는 관리자 API 주소의 앞부분 (이 주소로 시작하는 API 는 그 페이지의 것으로 본다)
// 관리자 유형(AdminType)마다 볼 수 있는 페이지가 다르다.
@Getter
@RequiredArgsConstructor
public enum AdminPage {
    DASHBOARD("대시보드", "/dashboard", "/api/v1/admin/dashboard"),
    VEHICLES("차량관리", "/dashboard/vehicles", "/api/v1/admin/vehicles"),
    DISPATCHES("배차관리", "/dashboard/dispatches", "/api/v1/admin/dispatches"),
    OPERATIONS("운행관리", "/dashboard/operations", "/api/v1/admin/operations"),
    COMPLAINTS("민원관리", "/dashboard/complaints", "/api/admin/complaints"),
    MEMBERS("회원관리", "/dashboard/members", "/api/v1/admin/members");

    // 모든 관리자가 쓰는 공통 API (사이드바의 내 정보 조회/수정). 페이지 권한과 상관없이 허용한다.
    private static final String COMMON_API = "/api/v1/admin/me";

    private final String label;
    private final String path;
    private final String apiPrefix;

    // 요청 주소가 어느 페이지의 API 인지 찾는다. 어느 페이지에도 속하지 않으면 비어 있다.
    //  - /api/v1/admin/vehicles, /api/v1/admin/vehicles/3 -> VEHICLES
    //  - /api/v1/admin/vehiclesX 처럼 이름만 비슷한 주소는 해당하지 않는다.
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
