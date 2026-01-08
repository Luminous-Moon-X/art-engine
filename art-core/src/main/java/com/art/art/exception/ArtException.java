package com.art.art.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Art平台异常类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class ArtException extends RuntimeException {
    /**
     * 异常编号
     */
    private int errorCode;
    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 抛出异常 指定异常信息
     *
     * @param message 异常信息
     */
    public ArtException(String message) {
        super(message);
        this.errorCode = 500;
        this.errorMessage = message;
    }

    /**
     * 抛出异常 带参数的异常信息
     *
     * @param message 带参数的异常信息
     * @param args    参数
     */
    public ArtException(String message, Object... args) {
        super(message);
        message = String.format(message.replace("{}", "%s"), args);
        this.errorCode = 500;
        this.errorMessage = message;
    }

    /**
     * 抛出异常 指定异常信息和异常编号
     *
     * @param message   异常信息
     * @param errorCode 异常编号
     */
    public ArtException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }
}
