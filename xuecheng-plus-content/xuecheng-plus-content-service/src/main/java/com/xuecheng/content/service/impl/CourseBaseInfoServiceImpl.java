package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
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

    private final String COURSE_CHARGE = "201001";

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        String courseName = queryCourseParamsDto.getCourseName();
        String auditStatus = queryCourseParamsDto.getAuditStatus();
        String publishStatus = queryCourseParamsDto.getPublishStatus();
        queryWrapper.like(StringUtils.isNotEmpty(courseName), CourseBase::getName, courseName);
        queryWrapper.like(StringUtils.isNotEmpty(auditStatus), CourseBase::getAuditStatus, auditStatus);
        queryWrapper.like(StringUtils.isNotEmpty(publishStatus), CourseBase::getStatus, publishStatus);

        //构建分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> pageInfo = courseBaseMapper.selectPage(page, queryWrapper);
        return new PageResult<>(pageInfo.getRecords(), pageInfo.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        //参数合法性校验
        //checkInvalid(addCourseDto);
        //封装课程基本信息
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());

        int baseInsert = courseBaseMapper.insert(courseBase);
        Long courseId = courseBase.getId();
        //封装课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        courseMarket.setId(courseId);
        //判断费用参数是否合法
        String charge = courseMarket.getCharge();
        if (COURSE_CHARGE.equals(charge)) {
            Float price = addCourseDto.getPrice();
            if (price == null || price <= 0) {
                throw new XueChengPlusException("课程设置了收费，价格不能为空，且必须大于0");
            }
        }
        int marketInsert = courseMarketMapper.insert(courseMarket);
        if (baseInsert <= 0 || marketInsert <= 0) {
            throw new XueChengPlusException("新增课程基本信息失败");
        }
        //组装返回添加的课程信息
        return getCourseBaseInfo(courseId);
    }

    private CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        return courseBaseInfoDto;
    }

    private void checkInvalid(AddCourseDto addCourseDto) {
        if (StringUtils.isBlank(addCourseDto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }
        if (StringUtils.isBlank(addCourseDto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }
        if (StringUtils.isBlank(addCourseDto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }
        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }
        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }
        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }
        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }
    }
}
