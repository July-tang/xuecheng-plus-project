package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.feign.client.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordDto;
import com.xuecheng.ucenter.model.dto.RegisterUserDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.XcUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author july
 */
@Slf4j
@Service
public class XcUserServiceImpl implements XcUserService {

    private static final String STUDENT_ROLE = "17";

    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    XcUserRoleMapper xcUserRoleMapper;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    CheckCodeClient checkCodeClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public XcUser register(RegisterUserDto registerUserDto) {
        if (!checkCodeClient.verify(registerUserDto.getCheckcodekey(), registerUserDto.getCheckcode())) {
            XueChengPlusException.cast("验证码错误！");
        }
        if (!registerUserDto.getConfirmpwd().equals(registerUserDto.getPassword())) {
            XueChengPlusException.cast("两次输入的密码不一致！");
        }
        String username = registerUserDto.getUsername();
        if (xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username)) != null) {
            XueChengPlusException.cast("该用户名已存在！");
        }
        String cellPhone = registerUserDto.getCellphone();
        if (xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getCellphone, cellPhone)) != null) {
            XueChengPlusException.cast("该手机号已被注册！");
        }
        //封装用户信息
        String userId = UUID.randomUUID().toString();
        XcUser xcUser = new XcUser();
        BeanUtils.copyProperties(registerUserDto, xcUser);
        xcUser.setId(userId);
        xcUser.setName(registerUserDto.getNickname());
        xcUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        xcUser.setCreateTime(LocalDateTime.now());
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        int userInsert = xcUserMapper.insert(xcUser);
        //封装用户角色关系信息
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId(STUDENT_ROLE);
        xcUserRole.setCreateTime(LocalDateTime.now());
        int userRoleInsert = xcUserRoleMapper.insert(xcUserRole);
        if (userInsert <= 0 || userRoleInsert <= 0) {
            throw new XueChengPlusException("注册失败！");
        }
        return xcUser;
    }

    @Override
    public XcUser updatePassword(FindPasswordDto passwordDto) {
        if (!checkCodeClient.verify(passwordDto.getCheckcodekey(), passwordDto.getCheckcode())) {
            XueChengPlusException.cast("验证码错误！");
        }
        if (!passwordDto.getConfirmpwd().equals(passwordDto.getPassword())) {
            XueChengPlusException.cast("两次输入的密码不一致！");
        }
        String cellphone = passwordDto.getCellphone();
        String email = passwordDto.getEmail();
        if (StringUtils.isEmpty(cellphone) && StringUtils.isEmpty(email)) {
            XueChengPlusException.cast("请输入手机号或者电子邮箱！");
        }
        XcUser user = null;
        if (StringUtils.isNotEmpty(cellphone)) {
            user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getCellphone, cellphone));
        } else {
            user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getEmail, email));
        }
        if (user == null) {
            XueChengPlusException.cast("该用户不存在！");
        }
        user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
        if (xcUserMapper.updateById(user) <= 0) {
            XueChengPlusException.cast("找回密码失败！");
        }
        return user;
    }
}
