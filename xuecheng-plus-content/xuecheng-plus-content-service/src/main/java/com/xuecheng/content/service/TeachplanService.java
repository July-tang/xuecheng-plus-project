package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachplanDto;

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

}
