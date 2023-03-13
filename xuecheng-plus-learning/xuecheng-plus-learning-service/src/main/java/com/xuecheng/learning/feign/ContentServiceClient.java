package com.xuecheng.learning.feign;

import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feign.fallback.ContentServiceClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 内容服务远程调用接口
 *
 * @author july
 */
@FeignClient(value = "content-api", fallbackFactory = ContentServiceClientFactory.class)
@RequestMapping("/content")
public interface ContentServiceClient {

    /**
     * 远程调用查询课程发布表
     *
     * @param courseId 课程id
     * @return 查询结果
     */
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId);
}
