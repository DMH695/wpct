package com.example.wpct.handler;

import com.example.wpct.utils.ResultBody;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Component
public class GlobalExceptionHandler {
    /**      方法参数校验     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultBody handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResultBody.fail("参数不正确");
    }
}
