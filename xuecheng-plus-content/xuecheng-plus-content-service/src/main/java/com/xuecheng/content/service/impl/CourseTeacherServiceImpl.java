package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author july
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Resource
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    public void saveCourseTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getId();
        if (id == null) {
            //新增
            courseTeacher.setCreateDate(LocalDateTime.now());
            if (courseTeacherMapper.insert(courseTeacher) <= 0) {
                XueChengPlusException.cast("新增课程教师失败！");
            }
        } else {
            if (courseTeacherMapper.updateById(courseTeacher) <= 0) {
                XueChengPlusException.cast("更新教师信息失败！");
            }
        }
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        if (courseTeacherMapper.deleteById(teacherId) <= 0) {
            XueChengPlusException.cast("教师不存在！");
        }
    }
}
