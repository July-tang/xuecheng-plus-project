package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author july
 */
@Slf4j
@Service
public class WxAuthServiceImpl implements AuthService {

    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    RestTemplate restTemplate;

    @Resource
    XcUserRoleMapper xcUserRoleMapper;

    @Resource
    WxAuthServiceImpl proxy;

    @Value("${wx.appid}")
    String appid;
    @Value("${wx.secret}")
    String secret;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }

    /**
     * 微信扫码验证
     *
     * @param code vx扫码后生成的code
     * @return XcUser 用户
     */
    public XcUser wxAuth(String code) {
        //收到code调用微信接口申请access_token
        Map<String, String> accessTokenMap = getAccessToken(code);
        if (accessTokenMap == null) {
            return null;
        }
        //获取用户信息
        String openid = accessTokenMap.get("openid");
        String accessToken = accessTokenMap.get("access_token");
        Map<String, String> userinfo = getUserinfo(accessToken, openid);
        if (userinfo == null) {
            return null;
        }
        return proxy.addWxUser(userinfo);
    }

    private Map<String, String> getUserinfo(String accessToken, String openid) {
        String wxUrlTemplate = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String wxUrl = String.format(wxUrlTemplate, accessToken, openid);
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.GET, null, String.class);
        if (exchange.getBody() == null) {
            log.debug("未收到微信返回的用户信息！");
            return null;
        }
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        log.info("调用微信接口获取用户信息: 返回值:{}", result);
        return JSON.parseObject(result, Map.class);
    }

    private Map<String, String> getAccessToken(String code) {
        String wxUrlTemplate = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String wxUrl = String.format(wxUrlTemplate, appid, secret, code);
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.GET, null, String.class);
        if (exchange.getBody() == null) {
            log.debug("未收到微信返回的access_token！");
            return null;
        }
        String result = exchange.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        return JSON.parseObject(result, Map.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public XcUser addWxUser(Map<String, String> userInfoMap) {
        String unionId = userInfoMap.get("unionid");
        //根据unionId查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionId));
        if (xcUser != null) {
            return xcUser;
        }
        //新增user
        xcUser = new XcUser();
        String userId = UUID.randomUUID().toString();
        xcUser.setId(userId);
        xcUser.setWxUnionid(unionId);
        //记录从微信得到的昵称
        xcUser.setNickname(userInfoMap.get("nickname"));
        xcUser.setUserpic(userInfoMap.get("headimgurl"));
        xcUser.setName(userInfoMap.get("nickname"));
        xcUser.setUsername(unionId);
        xcUser.setPassword(unionId);
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;
    }
}
