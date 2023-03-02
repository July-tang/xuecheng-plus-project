package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author july
 */
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    TeachplanService teachplanService;

    @Resource
    CourseTeacherService courseTeacherService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        List<CourseTeacher> teacherList = courseTeacherService.getCourseTeacherList(courseId);
        return new CoursePreviewDto(courseBaseInfo, teachplanTree, teacherList);
    }
}
