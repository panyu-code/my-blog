package com.panyu.mybolg.exception;

/**
 * 未授权异常，用于返回 HTTP 401
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
