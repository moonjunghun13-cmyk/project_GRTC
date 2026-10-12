package com.grtc.main.global.common;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Pages {

    private static final int MAX_SIZE = 100;

    private Pages() {
    }

    public static Pageable request(int page, int size, String sort, Sort defaultSort, Set<String> sortable) {
        int pageIndex = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(pageIndex, pageSize, parseSort(sort, defaultSort, sortable));
    }

    public static Pageable of(int page, int size, Sort sort) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(pageIndex, pageSize, sort);
    }

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
