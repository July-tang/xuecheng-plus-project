package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author july
 */
@Slf4j
@RestController
@RequestMapping("/course-category")
@Api(value = "课程分类相关接口", tags = "课程分类相关接口")
public class CourseCategoryController {

    @Resource
    CourseCategoryService courseCategoryService;

    @ApiOperation("课程分类相关接口")
    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }

}
