package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPasswordDto;
import com.xuecheng.ucenter.model.dto.RegisterUserDto;
import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @author july
 */
public interface XcUserService {

    /**
     * 注册用户
     *
     * @param registerUserDto 注册参数dto
     * @return XcUser 新用户
     */
    XcUser register(RegisterUserDto registerUserDto);

    /**
     * 更新密码
     *
     * @param passwordDto 请求参数dto
     * @return 更新后的用户
     */
    XcUser updatePassword(FindPasswordDto passwordDto);
}
