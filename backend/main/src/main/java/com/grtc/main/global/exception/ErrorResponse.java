package com.grtc.main.global.exception;

import java.util.List;

public record ErrorResponse(String code, String message, List<Detail> details) {

    public record Detail(String field, String message) {
    }
}
