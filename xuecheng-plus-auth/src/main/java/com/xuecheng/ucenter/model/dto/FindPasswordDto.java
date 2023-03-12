package com.xuecheng.ucenter.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author july
 */
@Data
@ApiModel(value="FindPasswordDto", description="找回密码请求信息")
public class FindPasswordDto {

    /**
     * 手机号
     */
    private String cellphone;
    /**
     * 电子邮箱
     */
    private String email;
    /**
     * 验证码key
     */
    private String checkcodekey;
    /**
     * 验证码
     */
    @NotEmpty(message = "验证码不能为空")
    private String checkcode;
    /**
     * 确认密码
     */
    @NotEmpty(message = "请确认密码")
    private String confirmpwd;
    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    private String password;
}
