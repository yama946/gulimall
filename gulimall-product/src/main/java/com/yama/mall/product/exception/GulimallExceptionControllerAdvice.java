package com.yama.mall.product.exception;

import com.yama.common.exception.BizCodeEnume;
import com.yama.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

/**
 * 集中处理异常
 */
//basePackages,指定异常出现的包
@RestControllerAdvice(basePackages = "com.yama.mall.product.controller")
@Slf4j
public class GulimallExceptionControllerAdvice {
    /**
     * MethodArgumentNotValidException是检验错误返回的异常，其中封装着BindingResult对象
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.debug("数据校验出现问题:{},异常类型:{}",e.getMessage(),e.getClass());
        //获取校验异常信息
        BindingResult bindingResult = e.getBindingResult();
        //自定义封装异常返回
        HashMap<String,String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError->{
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        }));
//        return R.error(400,"数据校验异常").put("data",errorMap);
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(),BizCodeEnume.VAILD_EXCEPTION.getMsg()).put("data",errorMap);
    }

    /**
     * 通用异常处理
     * @param throwable
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        throwable.printStackTrace();
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
