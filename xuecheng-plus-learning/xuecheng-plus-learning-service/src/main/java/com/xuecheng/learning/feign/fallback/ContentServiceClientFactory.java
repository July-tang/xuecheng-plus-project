package com.xuecheng.learning.feign.fallback;

import com.xuecheng.learning.feign.ContentServiceClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author july
 */
@Slf4j
@Component
public class ContentServiceClientFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return courseId -> {
            log.error("调用内容管理服务接口熔断:{}", throwable.getMessage());
            return null;
        };
    }
}
