package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author july
 */
@Slf4j
@RestController
@RequestMapping("/courseTeacher")
@Api(value = "教师信息相关接口", tags = "教师信息相关接口")
public class CourseTeacherController {

    @Resource
    private CourseTeacherService courseTeacherService;

    @GetMapping("/list/{courseId}")
    @ApiOperation("查询教师信息接口")
    public List<CourseTeacher> getCourseTeacherList(@PathVariable Long courseId) {
        return courseTeacherService.getCourseTeacherList(courseId);
    }

    @ApiOperation("添加/修改教师信息接口")
    @PostMapping
    public void saveCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        courseTeacherService.saveCourseTeacher(courseTeacher);
    }

    @ApiOperation("删除教师信息接口")
    @DeleteMapping("/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        courseTeacherService.deleteCourseTeacher(courseId, teacherId);
    }
}
