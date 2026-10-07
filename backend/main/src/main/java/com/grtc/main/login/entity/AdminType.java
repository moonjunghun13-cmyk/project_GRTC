package com.grtc.main.login.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// 관리자 유형과 유형별 열람 가능 페이지 (광주교통공사 임직원 세부사항 4. 사용자별 열람 가능 페이지 범위)
//   시스템 관리자 : 모든 페이지
//   부서장        : 대시보드
//   민원 담당자   : 대시보드, 민원관리
//   차량 담당자   : 대시보드, 차량관리
// ※ 회원 권한(Role)은 그대로 ADMIN 이고, 그 안에서 볼 수 있는 페이지만 이 유형으로 나눈다.
// ※ 페이지 권한을 바꾸려면 아래 목록만 고치면 된다. (API 차단과 로그인 응답의 pages 가 함께 바뀐다)
@Getter
public enum AdminType {
    SYSTEM_ADMIN("시스템 관리자", EnumSet.allOf(AdminPage.class)),
    DEPARTMENT_HEAD("부서장", EnumSet.of(AdminPage.DASHBOARD)),
    COMPLAINT_MANAGER("민원 담당자", EnumSet.of(AdminPage.DASHBOARD, AdminPage.COMPLAINTS)),
    VEHICLE_MANAGER("차량 담당자", EnumSet.of(AdminPage.DASHBOARD, AdminPage.VEHICLES));

    private final String label;
    private final Set<AdminPage> pages;

    AdminType(String label, Set<AdminPage> pages) {
        this.label = label;
        this.pages = pages;
    }

    // 열람 가능 페이지 (메뉴 순서대로)
    public List<AdminPage> pageList() {
        return Arrays.stream(AdminPage.values()).filter(pages::contains).toList();
    }

    public boolean canAccess(AdminPage page) {
        return pages.contains(page);
    }

    // 이 유형의 관리자가 해당 관리자 API 를 호출해도 되는지
    //  - 공통 API(내 정보)는 누구나 가능
    //  - 페이지에 속한 API 는 그 페이지를 볼 수 있을 때만 가능
    //  - 어느 페이지에도 속하지 않는 관리자 API 는 시스템 관리자만 가능 (새 API 를 만들고 등록을 잊어도 열리지 않게)
    public boolean canCallApi(String uri) {
        if (AdminPage.isCommonApi(uri)) {
            return true;
        }
        return AdminPage.ofApi(uri).map(this::canAccess).orElse(this == SYSTEM_ADMIN);
    }
}
