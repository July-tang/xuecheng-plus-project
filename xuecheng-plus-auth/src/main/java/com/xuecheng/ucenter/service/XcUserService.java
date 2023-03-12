package com.xuecheng.ucenter.service;

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
}
