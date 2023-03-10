package com.xuecheng.xuechengplusgateway.config;

import java.io.Serializable;

/**
 * 错误响应参数包装
 *
 * @author july
 */
public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
