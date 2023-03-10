package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.po.XcUser;
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
@Service
public class UserServiceImpl implements UserDetailsService {

    @Resource
    XcUserMapper xcUserMapper;

    /**
     * 根据账号查询用户信息
     * @param s 用户名
     * @return UserDetails用户信息
     * @throws UsernameNotFoundException 未找到用户异常
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, s));
        if (user == null) {
            return null;
        }
        String password = user.getPassword();
        String[] authorities= {"test"};
        //将用户信息转为JSON字符串充当用户名使之能携带其他用户信息
        user.setPassword(null);
        String userString = JSON.toJSONString(user);
        return User.withUsername(userString).password(password).authorities(authorities).build();
    }
}
