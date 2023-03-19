package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author july
 */
@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryMapper.selectTreeNodes(id+"%");
        Map<String, List<CourseCategoryTreeDto>> collect = categoryTreeDtos.stream().collect(Collectors.groupingBy(CourseCategory::getParentid));
        List<CourseCategoryTreeDto> result = collect.get(id);
        result.forEach(v -> v.setChildrenTreeNodes(collect.get(v.getId())));
        return result;
    }
}
