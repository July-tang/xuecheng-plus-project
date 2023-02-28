package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
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
public class TeachplanServiceImpl implements TeachplanService {

    @Resource
    private TeachplanMapper teachplanMapper;

    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTeachplan(Long teachplanId) {
        if (teachplanId == null) {
            XueChengPlusException.cast("课程计划不存在！");
        }
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, teachplanId);
        List<Teachplan> teachplanList = teachplanMapper.selectList(queryWrapper);
        if (teachplanList.size() > 0) {
            for (Teachplan teachplan : teachplanList) {
                teachplanMapper.deleteById(teachplan.getId());
                deleteMedias(teachplan.getId());
            }
        }
        teachplanMapper.deleteById(teachplanId);
        deleteMedias(teachplanId);
    }

    @Override
    public void moveUp(Long teachplanId) {
        changeOrder(teachplanId, -1);
    }

    @Override
    public void moveDown(Long teachplanId) {
        changeOrder(teachplanId, 1);
    }

    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade != 2) {
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //删除原媒资信息
        deleteMedias(teachplanId);
        //新增媒资关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    @Override
    public void deleteTeachplanMedia(Long teachPlanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId);
        mediaLambdaQueryWrapper.eq(TeachplanMedia::getMediaId, mediaId);
        if (teachplanMediaMapper.delete(mediaLambdaQueryWrapper) <= 0) {
            XueChengPlusException.cast("该课程计划或者视频不存在！");
        }
    }

    private void changeOrder(Long teachplanId, int d) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        int order = teachplan.getOrderby();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
        queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        queryWrapper.orderByAsc(Teachplan::getOrderby);
        List<Teachplan> teachplanList = teachplanMapper.selectList(queryWrapper);
        int left = 0, right = teachplanList.size() - 1;
        //二分查找课程计划所在的位置
        while (left < right) {
            int mid = (left + right + 1) >> 1;
            if (teachplanList.get(mid).getOrderby() <= order) {
                left = mid;
            } else {
                right = mid - 1;
            }
        }
        if (left + d < 0 || left + d >= teachplanList.size()) {
            XueChengPlusException.cast("该课程计划已处于顶部或者底部！");
        }
        Teachplan changedPlan = teachplanList.get(left + d);
        teachplan.setOrderby(changedPlan.getOrderby());
        changedPlan.setOrderby(order);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(changedPlan);
    }

    private void deleteMedias(Long teachplanId) {
        LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 删除teachplanId对应的所有媒资信息
        mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, teachplanId);
        teachplanMediaMapper.delete(mediaLambdaQueryWrapper);
    }

    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        queryWrapper.orderByDesc(Teachplan::getOrderby);
        List<Teachplan> list = teachplanMapper.selectList(queryWrapper);
        if (list.size() == 0) {
            return 0;
        }
        return list.get(0).getOrderby();
    }
}
