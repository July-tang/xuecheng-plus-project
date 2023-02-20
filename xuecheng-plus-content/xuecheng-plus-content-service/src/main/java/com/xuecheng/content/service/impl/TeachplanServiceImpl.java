package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
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
public class TeachplanServiceImpl implements TeachplanService {

    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTeachplan(TeachplanDto teachplanDto) {
        Long teachplanId = teachplanDto.getId();
        if (teachplanId == null) {
            //新增
            Teachplan plan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto, plan);
            plan.setCreateDate(LocalDateTime.now());
            plan.setOrderby(getTeachplanCount(plan.getCourseId(), plan.getParentid()) + 1);
            if (teachplanMapper.insert(plan) <= 0) {
                XueChengPlusException.cast("新增失败");
            }
        } else {
            //更新
            Teachplan plan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(teachplanDto, plan);
            plan.setChangeDate(LocalDateTime.now());
            if (teachplanMapper.updateById(plan) <= 0) {
                XueChengPlusException.cast("修改失败");
            }
        }
    }

    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }
}
