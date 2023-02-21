package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author july
 */
public interface CourseTeacherService {
    /**
     * 查询课程教师
     *
     * @param courseId 课程Id
     * @return 该课程下的老师列表
     */
    List<CourseTeacher> getCourseTeacherList(Long courseId);

    /**
     * 添加/更新教师信息
     *
     * @param courseTeacher 课程老师
     */
    void saveCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 删除课程教师
     *
     * @param courseId 课程Id
     * @param teacherId 教师Id
     */
    void deleteCourseTeacher(Long courseId, Long teacherId);
}
