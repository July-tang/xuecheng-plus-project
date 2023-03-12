package com.xuecheng.ucenter.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feign.client.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 账号密码认证
 *
 * @author july
 */
@Slf4j
@Service("password")
public class PasswordAuthServiceImpl implements AuthService {

    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //校验验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodeKey = authParamsDto.getCheckcodekey();
        if(StringUtils.isBlank(checkcodeKey) || StringUtils.isBlank(checkcode)){
            throw new RuntimeException("验证码为空");
        }
        if(!checkCodeClient.verify(checkcodeKey, checkcode)){
            throw new RuntimeException("验证码输入错误");
        }
        String username = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            throw new RuntimeException("该用户名不存在！");
        }
        //校验密码
        if(!passwordEncoder.matches(authParamsDto.getPassword(), user.getPassword())){
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }
}
