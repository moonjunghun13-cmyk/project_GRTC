package com.grtc.main.login.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
public enum AdminType {
    // 공지관리·자료실관리는 모든 관리자 유형이 쓸 수 있다 (작성·수정·삭제는 관리자만)
    SYSTEM_ADMIN("시스템 관리자", EnumSet.allOf(AdminPage.class)),
    DEPARTMENT_HEAD("부서장", EnumSet.of(AdminPage.DASHBOARD,
            AdminPage.NOTICES, AdminPage.ARCHIVES)),
    COMPLAINT_MANAGER("민원 담당자", EnumSet.of(AdminPage.DASHBOARD, AdminPage.COMPLAINTS,
            AdminPage.NOTICES, AdminPage.ARCHIVES)),
    VEHICLE_MANAGER("차량 담당자", EnumSet.of(AdminPage.DASHBOARD, AdminPage.VEHICLES, AdminPage.DISPATCHES,
            AdminPage.NOTICES, AdminPage.ARCHIVES));

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
    public boolean canCallApi(String uri) {
        if (AdminPage.isCommonApi(uri)) {
            return true;
        }
        return AdminPage.ofApi(uri).map(this::canAccess).orElse(this == SYSTEM_ADMIN);
    }
}
