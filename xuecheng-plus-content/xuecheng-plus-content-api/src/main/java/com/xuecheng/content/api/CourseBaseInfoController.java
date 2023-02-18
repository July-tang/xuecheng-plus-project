package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
/**
 * @description 课程信息编辑接口
 * @author July
 */
@Api(value = "课程管理接口",tags = "课程管理接口")
@RestController
public class CourseBaseInfoController {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(params, queryCourseParamsDto);
    }

    @ApiOperation("课程基本信息新增接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto) {
        return courseBaseInfoService.createCourseBase(37L, addCourseDto);
    }

    @ApiOperation("课程信息查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBase(@PathVariable Long courseId) {
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("课程信息修改接口")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {
        return courseBaseInfoService.updateCourseBase(37L, editCourseDto);
    }
}
