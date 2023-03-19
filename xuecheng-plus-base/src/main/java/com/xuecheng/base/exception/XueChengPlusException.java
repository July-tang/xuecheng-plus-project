package com.xuecheng.base.exception;

/**
 * 项目异常类
 *
 * @author july
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
