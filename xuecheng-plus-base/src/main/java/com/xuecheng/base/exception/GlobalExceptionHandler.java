package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * 全局异常处理器
 *
 * @author july
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    private static final String ACCESS_DENIED = "不允许访问";

    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doCustomException(XueChengPlusException exception) {
        log.error("系统异常：{}", exception.getMessage());
        return new RestErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<FieldError> errors = exception.getBindingResult().getFieldErrors();
        StringBuilder sb = new StringBuilder();
        errors.forEach(err -> {
            sb.append(err.getDefaultMessage()).append(",");
        });
        sb.setCharAt(sb.length() - 1, '！');
        return new RestErrorResponse(sb.toString());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doException(Exception exception) {
        log.error("系统异常：{}", exception.getMessage());
        if (ACCESS_DENIED.equals(exception.getMessage())) {
            return new RestErrorResponse("没有操作此功能的权限");
        }
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }
}
