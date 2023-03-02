package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 *
 * @author july
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "CoursePreviewDto", description = "课程预览数据模型")
public class CoursePreviewDto {

    /**
     * 课程基本信息与营销信息
     */
    CourseBaseInfoDto courseBase;

    /**
     * 课程计划信息
     */
    List<TeachplanDto> teachplans;

    /**
     * 课程教师
     */
    List<CourseTeacher> courseTeachers;
}
