package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

/**
 * @author july
 */
public interface LearningService {

    /**
     * 获取学习视频
     *
     * @param userId 用户id
     * @param courseId 课程id
     * @param teachplanId 课程计划id
     * @param mediaId 媒资id
     * @return RestResponse<String> 视频url
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
