package com.grtc.main.global.exception;

import com.grtc.main.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.stream.Collectors;

// 모든 오류를 명세서 3-1 의 실패 응답 형식으로 내려준다.
//   { "success": false, "data": null, "error": { "code", "message", "details" } }
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{

    // 서비스에서 던진 업무 오류 (ErrorCode 의 HTTP 상태 / code / message 그대로)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e){
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[BusinessException] {} - {}", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    // JSON 본문(@RequestBody) 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e){
        return invalidInput(e.getBindingResult());
    }

    // multipart 폼(@ModelAttribute) 검증 실패도 같은 형식으로 응답
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBind(BindException e){
        return invalidInput(e.getBindingResult());
    }

    // JSON 형식이 잘못됐거나 날짜/enum 값을 읽지 못할 때
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e){
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT.getCode(), "요청 형식이 올바르지 않습니다.", List.of()));
    }

    // ?status=ABC 처럼 쿼리 파라미터를 enum/숫자로 바꾸지 못할 때
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e){
        String message = ErrorCode.INVALID_PARAMETER.getMessage();
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_PARAMETER.getCode(), e.getName() + ": " + message,
                        List.of(new ErrorResponse.Detail(e.getName(), message))));
    }

    // 필수 쿼리 파라미터가 빠졌을 때
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e){
        String message = "필수 값입니다.";
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT.getCode(), e.getParameterName() + ": " + message,
                        List.of(new ErrorResponse.Detail(e.getParameterName(), message))));
    }

    // 파일 크기가 spring.servlet.multipart.max-file-size 를 넘으면 컨트롤러에 오기 전에 발생 (413)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUpload(MaxUploadSizeExceededException e){
        return ResponseEntity
                .status(ErrorCode.FILE_TOO_LARGE.getStatus())
                .body(ApiResponse.fail(ErrorCode.FILE_TOO_LARGE));
    }

    // 그 밖의 모든 오류
    //   - 스프링이 상태 코드를 정해 둔 오류(없는 주소 404, 허용되지 않은 메서드 405 등)는 그 상태 코드로,
    //   - 나머지는 500 INTERNAL_ERROR 로 내려준다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOthers(Exception e) throws Exception {
        // 인증/권한 오류는 스프링 시큐리티(SecurityConfig 의 401/403 처리)가 응답하도록 그대로 넘긴다.
        if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
            throw e;
        }
        if (e instanceof org.springframework.web.ErrorResponse standard) {
            int status = standard.getStatusCode().value();
            return ResponseEntity
                    .status(status)
                    .body(ApiResponse.fail(codeOf(status), messageOf(status), List.of()));
        }
        log.error("[Unhandled] {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR));
    }

    // 검증 실패 공통 응답: message 는 한 줄 요약, details 는 항목별 내용
    private ResponseEntity<ApiResponse<Void>> invalidInput(BindingResult bindingResult) {
        List<ErrorResponse.Detail> details = bindingResult.getFieldErrors().stream()
                .map(error -> new ErrorResponse.Detail(error.getField(), error.getDefaultMessage()))
                .toList();
        String message = details.isEmpty()
                ? ErrorCode.INVALID_INPUT.getMessage()
                : details.stream()
                        .map(detail -> detail.field() + ": " + detail.message())
                        .collect(Collectors.joining(", "));
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT.getCode(), message, details));
    }

    private static String codeOf(int status) {
        return switch (status) {
            case 400 -> "INVALID_INPUT";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "RESOURCE_NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 413 -> "FILE_TOO_LARGE";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> status >= 500 ? "INTERNAL_ERROR" : "INVALID_INPUT";
        };
    }

    private static String messageOf(int status) {
        return switch (status) {
            case 401 -> "로그인이 필요합니다.";
            case 403 -> "권한이 없습니다.";
            case 404 -> "요청한 대상을 찾을 수 없습니다.";
            case 405 -> "허용되지 않은 요청 방식입니다.";
            case 413 -> "파일 크기가 너무 큽니다.";
            case 415 -> "지원하지 않는 요청 형식입니다.";
            default -> status >= 500 ? "서버 오류가 발생했습니다." : "요청값이 올바르지 않습니다.";
        };
    }
}
