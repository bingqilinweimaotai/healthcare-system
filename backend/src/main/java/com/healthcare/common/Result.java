package com.healthcare.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口返回体。
 *
 * 约定：
 * - code：业务/HTTP语义码（通常与HTTP状态一致，如 200/400/401/403/500）
 * - message：提示信息
 * - data：业务数据（可为 null）
 * - errors：校验错误等附加信息（可为 null）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private Object errors;

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "OK", data, null);
    }

    public static Result<Void> ok() {
        return new Result<>(200, "OK", null, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, null);
    }

    public static <T> Result<T> fail(int code, String message, Object errors) {
        return new Result<>(code, message, null, errors);
    }
}

