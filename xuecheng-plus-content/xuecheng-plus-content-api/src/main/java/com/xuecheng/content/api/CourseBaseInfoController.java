package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.utils.SecurityUtil;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
/**
 * @description 课程信息编辑接口
 * @author July
 */
@Slf4j
@RestController
@RequestMapping("/course")
@Api(value = "课程管理接口",tags = "课程管理接口")
public class CourseBaseInfoController {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程列表查询接口")
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(params, queryCourseParamsDto);
    }

    @ApiOperation("课程信息新增接口")
    @PostMapping
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto) {
        Long companyId = 37L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("课程信息查询接口")
    @GetMapping("/{courseId}")
    public CourseBaseInfoDto getCourseBase(@PathVariable Long courseId) {
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("课程信息修改接口")
    @PutMapping
    public CourseBaseInfoDto updateCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {
        Long companyId = 37L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("课程信息删除接口")
    @DeleteMapping("/{courseId}")
    public void deleteCourseBase(@PathVariable Long courseId) {
        Long companyId = 37L;
        courseBaseInfoService.deleteCourseBase(companyId, courseId);
    }
}
