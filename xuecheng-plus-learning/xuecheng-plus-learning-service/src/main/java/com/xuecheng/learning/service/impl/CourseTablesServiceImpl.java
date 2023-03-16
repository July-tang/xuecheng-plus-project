package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.enums.StatusCodeEnum;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feign.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.CourseTablesService;
import com.xuecheng.messagesdk.config.RabbitMqConfig;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author july
 */
@Slf4j
@Service
public class CourseTablesServiceImpl implements CourseTablesService {

    @Resource
    XcChooseCourseMapper chooseCourseMapper;

    @Resource
    XcCourseTablesMapper courseTablesMapper;

    @Resource
    ContentServiceClient contentServiceClient;

    @Resource
    MqMessageService mqMessageService;

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
            XcCourseTables courseTables = proxy.addCourseTables(xcChooseCourse);
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

    @Override
    public PageResult<XcCourseTables> getCourseTablePage(MyCourseTableParams params) {
        //页码
        long pageNo = params.getPage();
        //每页记录数, 固定为4
        long pageSize = 4;
        Page<XcCourseTables> page = new Page<>(pageNo, pageSize);
        //根据用户id查询
        String userId = params.getUserId();
        LambdaQueryWrapper<XcCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId);
        //分页查询
        Page<XcCourseTables> pageResult = courseTablesMapper.selectPage(page, lambdaQueryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageNo, pageSize);
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
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
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
        if (chooseCourseMapper.insert(xcChooseCourse) <= 0) {
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
        if (courseTablesMapper.insert(xcCourseTable) <= 0) {
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
        return courseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
    }

    @RabbitListener(queues = RabbitMqConfig.PAY_NOTIFY_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void receive(Message message) {
        MqMessage mqMessage = JSON.parseObject(new String(message.getBody()), MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);

        //选课记录id
        String chooseCourseId  = mqMessage.getBusinessKey1();
        XcChooseCourse chooseCourse = chooseCourseMapper.selectById(chooseCourseId);
        if (chooseCourse != null) {
            if (!StatusCodeEnum.WAIT_PAY.getCode().equals(chooseCourse.getStatus())) {
                log.debug("该课程已完成选课：{}", chooseCourse);
                return;
            }
            chooseCourse.setStatus(StatusCodeEnum.CHOOSE_SUCCESS.getCode());
            chooseCourseMapper.updateById(chooseCourse);
            proxy.addCourseTables(chooseCourse);
        }
    }
}
