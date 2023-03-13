package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.enums.DictionaryCode;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
/**
 * @author july
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketService courseMarketService;

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Resource
    CourseTeacherMapper courseTeacherMapper;

    @Resource
    TeachplanMapper teachplanMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        String courseName = queryCourseParamsDto.getCourseName();
        String auditStatus = queryCourseParamsDto.getAuditStatus();
        String publishStatus = queryCourseParamsDto.getPublishStatus();
        queryWrapper.like(StringUtils.isNotEmpty(courseName), CourseBase::getName, courseName);
        queryWrapper.like(StringUtils.isNotEmpty(auditStatus), CourseBase::getAuditStatus, auditStatus);
        queryWrapper.like(StringUtils.isNotEmpty(publishStatus), CourseBase::getStatus, publishStatus);
        queryWrapper.eq(CourseBase::getCompanyId, companyId);

        //构建分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> pageInfo = courseBaseMapper.selectPage(page, queryWrapper);
        return new PageResult<>(pageInfo.getRecords(), pageInfo.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        //封装课程基本信息
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setAuditStatus(DictionaryCode.AUDIT_NOT_SUBMIT);
        courseBase.setStatus(DictionaryCode.COURSE_NOT_SUBMIT);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());

        int baseInsert = courseBaseMapper.insert(courseBase);
        Long courseId = courseBase.getId();
        //封装课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        if (courseMarket.getValidDays() == null) {
            courseMarket.setValidDays(365);
        }
        courseMarket.setId(courseId);
        //保存课程营销信息
        int marketInsert = saveCourseMarket(courseMarket);
        if (baseInsert <= 0 || marketInsert <= 0) {
            throw new XueChengPlusException("新增课程信息失败");
        }
        //组装返回添加的课程信息
        return getCourseBaseInfo(courseId);
    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketService.getById(courseId);
        //组装课程基本信息
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            //组装课程营销信息
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        //组装课程分类信息
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketService.getById(courseId);
//        if (!companyId.equals(courseBase.getCompanyId())) {
//            XueChengPlusException.cast("只允许修改本机构的课程！");
//        }
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        if (courseMarket == null) {
            courseMarket = new CourseMarket();
        }
        courseMarket.setId(courseId);
        // 获取课程收费状态并设置
        courseMarket.setCharge(editCourseDto.getCharge());
        // 对象拷贝
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        // 有则更新，无则插入
        int marketUpdate = saveCourseMarket(courseMarket);
        int baseUpdate = courseBaseMapper.updateById(courseBase);
        if (baseUpdate <= 0 || marketUpdate <= 0) {
            throw new XueChengPlusException("修改课程信息失败");
        }
        return getCourseBaseInfo(courseId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCourseBase(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
//        if (!companyId.equals(courseBase.getCompanyId())) {
//            XueChengPlusException.cast("只允许修改本机构的课程！");
//        }
        //删除课程计划信息
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(wrapper);
        //删除课程老师信息
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(queryWrapper);
        //删除课程营销信息
        courseMarketService.removeById(courseId);
        //删除课程基本信息
        courseBaseMapper.deleteById(courseId);
    }


    private int saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)) {
            XueChengPlusException.cast("请设置收费规则");
        }
        // 如果课程收费，则判断价格是否正常
        if (DictionaryCode.COURSE_CHARGE.equals(charge)) {
            Float price = courseMarket.getPrice();
            if (price == null || price <= 0) {
                XueChengPlusException.cast("课程设置了收费，价格不能为空，且必须大于0");
            }
        }
        return courseMarketService.saveOrUpdate(courseMarket) ? 1 : -1;
    }
}
