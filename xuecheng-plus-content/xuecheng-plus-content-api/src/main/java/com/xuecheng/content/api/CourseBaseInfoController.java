package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 课程信息编辑接口
 *
 * @author July
 */
@Slf4j
@RestController
@RequestMapping("/course")
@Api(value = "课程管理接口", tags = "课程管理接口")
public class CourseBaseInfoController {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程列表查询接口")
    @PreAuthorize("hasAnyAuthority('xc_teachmanager_course_list')")
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        Long companyId = UserUtil.getCompanyId();
        return courseBaseInfoService.queryCourseBaseList(companyId, params, queryCourseParamsDto);
    }

    @ApiOperation("课程信息新增接口")
    @PostMapping
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto) {
        Long companyId = UserUtil.getCompanyId();
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
        Long companyId = UserUtil.getCompanyId();
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("课程信息删除接口")
    @DeleteMapping("/{courseId}")
    public void deleteCourseBase(@PathVariable Long courseId) {
        Long companyId = UserUtil.getCompanyId();
        courseBaseInfoService.deleteCourseBase(companyId, courseId);
    }
}
