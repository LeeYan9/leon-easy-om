package com.lyon.easy.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lyon
 */
@SuppressWarnings({"unused", "AlibabaClassNamingShouldBeCamel"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    private int code;

    private T data;

    private Throwable throwable;


    private String message;

    private static final int SUCCESS = 200;

    private static final int ERROR = 500;


    public static <T> R<T> success(T data) {
        return result(SUCCESS, null, data);
    }

    public static <T> R<T> failed(Throwable throwable) {
        return new R<T>(ERROR, null, throwable,null);
    }


    public static <T> R<T> success(String message, T data) {
        return result(SUCCESS, message, data);
    }

    public static <T> R<T> failed(String message) {
        return result(ERROR, message, null);
    }

    public static <T> R<T> failed(String message, T data) {
        return result(ERROR, message, data);
    }

    public static <T> R<T> failed(int code, String message, T data) {
        return result(code, message, data);
    }

    private static <T> R<T> result(int code, String message, T data) {
        return new R<T>(code, data, null,message);
    }

}
