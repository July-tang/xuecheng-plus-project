package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户信息服务
 *
 * @author july
 */
@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    ApplicationContext applicationContext;

    /**
     * 查询用户信息
     * @param s AuthParamsDto类型的json数据
     * @return UserDetails用户信息
     * @throws UsernameNotFoundException 未找到用户异常
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}",s);
            throw new RuntimeException("认证请求数据格式有误！");
        }
        AuthService authService =  applicationContext.getBean(authParamsDto.getAuthType(), AuthService.class);
        XcUserExt user = authService.execute(authParamsDto);
        return getUserPrincipal(user);
    }

    private UserDetails getUserPrincipal(XcUserExt user) {
        String[] authorities = {"test"};
        String password = user.getPassword();
        user.setPassword(null);
        String userString = JSON.toJSONString(user);
        return User.withUsername(userString).password(password).authorities(authorities).build();
    }
}
