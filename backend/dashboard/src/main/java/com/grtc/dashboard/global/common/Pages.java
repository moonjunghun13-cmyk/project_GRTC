package com.grtc.dashboard.global.common;

import com.grtc.dashboard.global.exception.BusinessException;
import com.grtc.dashboard.global.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// page/size/sort 요청값을 안전한 Pageable 로 바꿔주는 유틸
public final class Pages {

    private static final int MAX_SIZE = 100; // 한 번에 너무 많이 가져가지 못하도록 상한

    private Pages() {
    }

    // [명세서 기준] ?page=0&size=20&sort=createdAt,desc  (page 는 0부터)
    //   sort        : "필드,방향" 형식. 방향을 빼면 asc. 여러 개면 sort 를 반복해서 보낸다.
    //   defaultSort : sort 를 보내지 않았을 때의 정렬
    //   sortable    : 정렬을 허용하는 필드 이름. 여기에 없는 필드를 보내면 400 INVALID_INPUT
    public static Pageable request(int page, int size, String sort, Sort defaultSort, Set<String> sortable) {
        int pageIndex = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(pageIndex, pageSize, parseSort(sort, defaultSort, sortable));
    }

    // [예전 방식] 화면(1부터 시작) 기준 page/size (아직 예전 주소를 쓰는 민원관리에서만 사용)
    public static Pageable of(int page, int size, Sort sort) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(pageIndex, pageSize, sort);
    }

    // "createdAt,desc" 또는 "dispatchDate,desc,dispatchNo,asc" 를 Sort 로 바꾼다.
    private static Sort parseSort(String sort, Sort defaultSort, Set<String> sortable) {
        if (sort == null || sort.isBlank()) {
            return defaultSort;
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String token : sort.split(",")) {
            String value = token.trim();
            if (value.isEmpty()) {
                continue;
            }
            boolean asc = value.equalsIgnoreCase("asc");
            boolean desc = value.equalsIgnoreCase("desc");
            if (asc || desc) {
                // 방향은 바로 앞에 나온 필드에 적용한다.
                if (orders.isEmpty()) {
                    throw new BusinessException(ErrorCode.INVALID_PARAMETER);
                }
                String property = orders.remove(orders.size() - 1).getProperty();
                orders.add(desc ? Sort.Order.desc(property) : Sort.Order.asc(property));
            } else {
                if (!sortable.contains(value)) {
                    throw new BusinessException(ErrorCode.INVALID_PARAMETER);
                }
                orders.add(Sort.Order.asc(value));
            }
        }
        return orders.isEmpty() ? defaultSort : Sort.by(orders);
    }
}
