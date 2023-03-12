package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.impl.WxAuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * vx扫码登陆controller
 *
 * @author july
 */
@Slf4j
@Controller
public class WxLoginController {

    @Resource
    WxAuthServiceImpl wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) {
        log.debug("微信扫码回调, code:{}, state:{}", code, state);
        XcUser xcUser = wxAuthService.wxAuth(code);
        if (xcUser == null) {
            return "redirect:http://www.51xuecheng.com/error.html";
        }
        return "redirect:http://www.51xuecheng.com/sign.html?username=" +  xcUser.getUsername() + "&authType=wx";
    }

}
