package com.xuecheng.learning.feign.fallback;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.feign.MediaServiceClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author july
 */
@Slf4j
@Component
public class MediaServiceClientFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return mediaId -> {
            log.error("调用媒资管理服务接口熔断:{}", throwable.getMessage());
            return null;
        };
    }
}
