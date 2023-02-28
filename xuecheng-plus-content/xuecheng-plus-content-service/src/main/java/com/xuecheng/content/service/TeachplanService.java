package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

/**
 * @author july
 */
public interface TeachplanService {

    /**
     * 查询课程计划树形结构
     * @param courseId 课程Id
     * @return 课程计划dto列表
     */
    List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 保存课程计划
     * @param teachplanDto 课程计划dto
     */
    void saveTeachplan(TeachplanDto teachplanDto);

    /**
     * 删除课程计划
     * @param teachplanId 课程计划Id
     */
    void deleteTeachplan(Long teachplanId);

    /**
     * 课程计划上移
     *
     * @param teachplanId 课程计划Id
     */
    void moveUp(Long teachplanId);

    /**
     * 课程计划下移
     *
     * @param teachplanId 课程计划Id
     */
    void moveDown(Long teachplanId);

    /**
     * 课程计划绑定媒资
     *
     * @param bindTeachplanMediaDto 绑定关系dto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     */
    TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 课程计划删除媒资
     *
     * @param teachPlanId 课程计划Id
     * @param mediaId 媒资Id
     */
    void deleteTeachplanMedia(Long teachPlanId, String mediaId);
}
