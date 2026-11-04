package com.rtb.common;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 手动抛出的BizException
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> onBiz(BizException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(e.getStatus(), e.getMessage()));
    }

    // @RequestBody和@ModelAttribute中转换成 DTO，但字段校验失败(Valid未通过)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> onValidation(
            BindException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + " " + error.getDefaultMessage()
                )
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, message));
    }


    // @RequestBody中JSON 格式错误、无法反序列化为Java bean（数据类型冲突）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> onUnreadable(
            HttpMessageNotReadableException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, "请求体缺失或 JSON 格式不正确"));
    }

    // @RequestParams和@PathVariable的错误
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> onTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        400,
                        "参数 " + e.getName() + " 类型不正确"
                ));
    }

    // 用 text/plain 发送本应是 JSON 的请求 (Content-Type错误)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> onUnsupportedMediaType(
            HttpMediaTypeNotSupportedException e
    ) {
        return ResponseEntity
                .status(415)
                .body(ApiResponse.error(
                        415,
                        "不支持该 Content-Type，请使用 application/json"
                ));
    }
}
