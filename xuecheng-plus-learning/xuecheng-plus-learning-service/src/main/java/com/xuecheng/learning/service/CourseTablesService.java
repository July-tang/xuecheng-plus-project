package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @author july
 */
public interface CourseTablesService {

    /**
     * 添加选课
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcChooseCourseDto
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 查询学习资格
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcCourseTablesDto
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 分页查询课程表
     *
     * @param params 分页参数dto
     * @return PageResult<XcCourseTables> 分页结果
     */
    PageResult<XcCourseTables> getCourseTablePage(MyCourseTableParams params);
}
