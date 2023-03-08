package com.xuecheng.content.feign.client;

import com.xuecheng.content.feign.fallback.SearchServiceClientFallbackFactory;
import com.xuecheng.search.po.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 搜索服务远程接口
 *
 * @author july
 */
@FeignClient(value = "search", fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {

    /**
     * 添加索引
     * @param courseIndex 索引po
     * @return 添加结果
     */
    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);

}
