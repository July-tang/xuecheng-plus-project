package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @author july
 * @description 课程基本信息管理业务接口
 */
public interface CourseBaseInfoService {

    /**
     * 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 分页条件
     * @return <com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams,
                                               QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 课程基本信息新增接口
     * @param companyId 机构Id
     * @param addCourseDto 新增课程的dto
     * @return 课程基本信息dto
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程基本信息
     * @param courseId 课程Id
     * @return 课程基本信息dto
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程基本信息
     * @param companyId 机构Id,本机构只能修改本机构课程
     * @param editCourseDto 待保存的修改后的课程信息
     * @return 课程基本信息dto
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);
}
