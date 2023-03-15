package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.enums.StatusCodeEnum;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feign.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.CourseTablesService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author july
 */
@Service
public class CourseTablesServiceImpl implements CourseTablesService {

    @Resource
    XcChooseCourseMapper xcChooseCourseMapper;

    @Resource
    XcCourseTablesMapper xcCourseTablesMapper;

    @Resource
    ContentServiceClient contentServiceClient;

    @Resource
    CourseTablesServiceImpl proxy;

    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        CoursePublish coursepublish = contentServiceClient.getCoursePublish(courseId);
        if (coursepublish == null) {
            XueChengPlusException.cast("该课程已不存在！");
        }
        //新增选课记录
        XcChooseCourse xcChooseCourse;
        if (StatusCodeEnum.FREE.getCode().equals(coursepublish.getCharge())) {
            xcChooseCourse = proxy.chooseCourse(userId, coursepublish, StatusCodeEnum.FREE_COURSE.getCode());
            proxy.addCourseTables(xcChooseCourse);
        } else {
            xcChooseCourse = proxy.chooseCourse(userId, coursepublish, StatusCodeEnum.CHARGE_COURSE.getCode());
        }
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        //查询学习资格
        XcCourseTablesDto xcCourseTablesDto = proxy.getLearningStatus(userId, courseId);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        return xcChooseCourseDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if (xcCourseTables == null) {
            XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
            //没有选课或选课后没有支付
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        if (xcCourseTablesDto.getValidtimeEnd().isAfter(LocalDateTime.now())) {
            xcCourseTablesDto.setLearnStatus(StatusCodeEnum.NORMAL_STUDY.getCode());
        } else {
            xcCourseTablesDto.setLearnStatus(StatusCodeEnum.EXPIRED.getCode());
        }
        return xcCourseTablesDto;
    }

    /**
     * 添加选课记录
     *
     * @param userId        用户id
     * @param coursePublish 课程信息
     * @param chargeState   课程收费状态
     * @return 选课信息
     */
    @Transactional(rollbackFor = Exception.class)
    public XcChooseCourse chooseCourse(String userId, CoursePublish coursePublish, String chargeState) {
        String successState;
        if (StatusCodeEnum.FREE_COURSE.getCode().equals(chargeState)) {
            successState = StatusCodeEnum.CHOOSE_SUCCESS.getCode();
        } else {
            successState = StatusCodeEnum.WAIT_PAY.getCode();
        }
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                .eq(XcChooseCourse::getOrderType, chargeState)
                .eq(XcChooseCourse::getStatus, successState);
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size() > 0) {
            return xcChooseCourses.get(0);
        }
        //新增选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursePublish.getId());
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setCoursePrice(coursePublish.getPrice() == null ? 0f : coursePublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType(chargeState);
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus(successState);
        xcChooseCourse.setValidDays(coursePublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursePublish.getValidDays()));
        if (xcChooseCourseMapper.insert(xcChooseCourse) <= 0) {
            XueChengPlusException.cast("新增选课记录失败！");
        }
        return xcChooseCourse;
    }

    /**
     * 根据选课记录将课程添加到课程表
     *
     * @param xcChooseCourse 选课记录
     * @return 课程表记录
     */
    @Transactional(rollbackFor = Exception.class)
    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse) {
        String status = xcChooseCourse.getStatus();
        if (!StatusCodeEnum.CHOOSE_SUCCESS.getCode().equals(status)) {
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        XcCourseTables xcCourseTable = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (xcCourseTable != null) {
            return xcCourseTable;
        }
        xcCourseTable = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTable);
        xcCourseTable.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTable.setUpdateDate(LocalDateTime.now());
        xcCourseTable.setCreateDate(LocalDateTime.now());
        xcCourseTable.setCourseType(xcChooseCourse.getOrderType());
        if (xcCourseTablesMapper.insert(xcCourseTable) <= 0) {
            XueChengPlusException.cast("添加到课程表失败！");
        }
        return xcCourseTable;
    }

    /**
     * 根据课程和用户查询课程表中某一门课程
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return XcCourseTables 课程
     */
    private XcCourseTables getXcCourseTables(String userId, Long courseId) {
        return xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
    }

}
