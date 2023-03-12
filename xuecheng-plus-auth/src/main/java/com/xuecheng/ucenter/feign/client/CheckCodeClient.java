package com.xuecheng.ucenter.feign.client;

import com.xuecheng.ucenter.feign.fallback.CheckCodeClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 搜索服务远程接口
 *
 * @author july
 */
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeClientFallbackFactory.class)
public interface CheckCodeClient {

    /**
     * 校验验证码
     *
     * @param key 验证码key
     * @param code 验证码
     * @return 校验结果
     */
    @PostMapping(value = "/checkcode/verify")
    Boolean verify(@RequestParam("key") String key, @RequestParam("code")String code);
}