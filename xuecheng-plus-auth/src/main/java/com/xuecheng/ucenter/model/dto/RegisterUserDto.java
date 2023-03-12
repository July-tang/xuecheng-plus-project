package com.xuecheng.ucenter.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 用户注册参数dto
 *
 * @author july
 */
@Data
@ApiModel(value="RegisterUserDto", description="注册用户基本信息")
public class RegisterUserDto {

    /**
     * 手机号
     */
    @NotEmpty(message = "手机号不能为空")
    @ApiModelProperty(value = "手机号", required = true)
    private String cellphone;
    /**
     * 验证码
     */
    @NotEmpty(message = "验证码不能为空")
    @ApiModelProperty(value = "验证码", required = true)
    private String checkcode;
    /**
     * 验证码key
     */
    @NotEmpty(message = "验证码key不能为空")
    @ApiModelProperty(value = "验证码key", required = true)
    private String checkcodekey;
    /**
     * 确认密码
     */
    @NotEmpty(message = "请确认密码")
    @ApiModelProperty(value = "确认密码", required = true)
    private String confirmpwd;
    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;
    /**
     * 昵称
     */
    @NotEmpty(message = "昵称不能为空")
    @ApiModelProperty(value = "昵称", required = true)
    private String nickname;
    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true)
    private String password;
    /**
     * 账号
     */
    @NotEmpty(message = "账号不能为空")
    @ApiModelProperty(value = "账号", required = true)
    private String username;
}
