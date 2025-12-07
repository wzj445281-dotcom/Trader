package com.trader.app.config;

import com.trader.app.util.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        logger.error("Unhandled exception: ", e);
        return Result.fail("Internal Server Error: " + e.getMessage());
    }

    // 可以添加自定义业务异常
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleArgsException(IllegalArgumentException e) {
        return Result.fail(e.getMessage());
    }
}