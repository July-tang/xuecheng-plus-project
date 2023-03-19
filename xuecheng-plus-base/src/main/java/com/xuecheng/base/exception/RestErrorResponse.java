package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 异常响应体
 *
 * @author july
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestErrorResponse implements Serializable {
    private String errMessage;
}
