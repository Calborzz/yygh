package com.atguigu.yygh.common.exception;


import com.atguigu.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public R error(Exception e){
        e.printStackTrace();
        return R.error();
    }

    @ExceptionHandler(RuntimeException.class)
    public R runtimeException(RuntimeException e){
        e.printStackTrace();
        return R.error().message(e.toString());
    }


}
