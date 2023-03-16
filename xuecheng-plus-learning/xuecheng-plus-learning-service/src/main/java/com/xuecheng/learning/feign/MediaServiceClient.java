package com.xuecheng.learning.feign;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.feign.fallback.MediaServiceClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author july
 */
@FeignClient(value = "media-api", fallbackFactory = MediaServiceClientFactory.class)
public interface MediaServiceClient {

    /**
     * 查询媒资url
     *
     * @param mediaId 媒资id
     * @return RestResponse<String>
     */
    @GetMapping("/media/open/preview/{mediaId}")
    RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId);
}
