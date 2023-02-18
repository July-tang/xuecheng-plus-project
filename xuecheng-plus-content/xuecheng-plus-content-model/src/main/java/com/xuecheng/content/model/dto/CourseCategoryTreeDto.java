package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author july
 * @description 课程分类节点
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourseCategoryTreeDto extends CourseCategory {
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
