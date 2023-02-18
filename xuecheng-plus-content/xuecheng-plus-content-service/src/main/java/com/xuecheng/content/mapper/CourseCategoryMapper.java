package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author july
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    /**
     * 课程分类查询
     * @param id 根节点ID
     * @return
     */
    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
