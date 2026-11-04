package com.rtb.common;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final int status;

    public BizException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static BizException notFound(String message) {
        return new BizException(404, message);
    }
}
