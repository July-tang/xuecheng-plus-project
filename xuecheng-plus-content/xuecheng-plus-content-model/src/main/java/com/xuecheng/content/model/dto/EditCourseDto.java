package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author july
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EditCourseDto extends AddCourseDto {
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}
