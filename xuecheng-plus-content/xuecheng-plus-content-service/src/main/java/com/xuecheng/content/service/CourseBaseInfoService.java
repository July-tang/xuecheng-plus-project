package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
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
     * 新增课程基本信息接口
     * @param companyId 机构Id
     * @param addCourseDto 新增课程dto
     * @return 课程基本信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);
}
