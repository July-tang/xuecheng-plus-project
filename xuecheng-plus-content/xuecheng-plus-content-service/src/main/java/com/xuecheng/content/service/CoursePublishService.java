package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * @author july
 */
public interface CoursePublishService {

    /**
     * 获取课程预览信息
     *
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     *
     * @param companyId 机构Id
     * @param courseId  课程Id
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布
     *
     * @param companyId 机构id
     * @param courseId  课程id
     */
    void publish(Long companyId, Long courseId);


    /**
     * 课程静态化
     *
     * @param courseId 课程id
     * @return File 静态化文件
     */
    File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     *
     * @param courseId 课程Id
     * @param file     静态化文件
     */
    void uploadCourseHtml(Long courseId, File file);

    /**
     * 保存课程索引信息
     *
     * @param courseId 课程Id
     * @return Boolean 响应结果
     */
    Boolean saveCourseIndex(Long courseId);

    /**
     * 查询课程发布表
     *
     * @param courseId 课程Id
     * @return 查询结果
     */
    CoursePublish getCoursePublish(Long courseId);
}
