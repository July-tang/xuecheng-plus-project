package com.xuecheng.ucenter.feign.fallback;

import com.xuecheng.ucenter.feign.client.CheckCodeClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author july
 */
@Slf4j
@Component
public class CheckCodeClientFallbackFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return (key, code) -> {
            log.debug("远程调用验证码模块熔断异常：{}", throwable.getMessage());
            return null;
        };
    }
}
