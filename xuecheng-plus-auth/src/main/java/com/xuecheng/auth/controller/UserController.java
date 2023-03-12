package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.dto.RegisterUserDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.XcUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 用户相关controller
 *
 * @author july
 */
@Slf4j
@Controller
public class UserController {

    @Resource
    XcUserService xcUserService;

    @ResponseBody
    @PostMapping("/register")
    public XcUser register(@RequestBody RegisterUserDto registerUserDto) {
        return xcUserService.register(registerUserDto);
    }
}
