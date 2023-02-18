package com.xuecheng.base.exception;

/**
 * @author july
 * @description 项目异常类
 */
public class XueChengPlusException extends RuntimeException {

    private String message;

    @Override
    public String getMessage() {
        return message;
    }

    public XueChengPlusException() {
        super();
    }

    public XueChengPlusException(String message) {
        super(message);
        this.message = message;
    }

    public static void cast(CommonError commonError) {
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new XueChengPlusException(errMessage);
    }
}
