package com.xuecheng.learning.service.impl;

import com.xuecheng.base.enums.StatusCodeEnum;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feign.ContentServiceClient;
import com.xuecheng.learning.feign.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.CourseTablesService;
import com.xuecheng.learning.service.LearningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author july
 */
@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Resource
    CourseTablesService courseTablesService;

    @Resource
    ContentServiceClient contentServiceClient;

    @Resource
    MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        CoursePublish coursepublish = contentServiceClient.getCoursePublish(courseId);
        if (coursepublish == null) {
            XueChengPlusException.cast("课程信息不存在");
        }
        String charge = coursepublish.getCharge();
        if(!StatusCodeEnum.FREE.getCode().equals(charge)){
            //校验学习资格
            String learningStatus = courseTablesService.getLearningStatus(userId, courseId).getLearnStatus();
            if (!StatusCodeEnum.NORMAL_STUDY.getCode().equals(learningStatus)) {
                return RestResponse.validfail("请购买课程后继续学习");
            }
        }
        return mediaServiceClient.getPlayUrlByMediaId(mediaId);
    }
}
