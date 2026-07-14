package com.y1fel.backend.common.exception;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
public class BizException extends RuntimeException {
    private final Integer code;

    public BizException(String message) {
        super(message);
        this.code = 500;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}//逻辑异常
