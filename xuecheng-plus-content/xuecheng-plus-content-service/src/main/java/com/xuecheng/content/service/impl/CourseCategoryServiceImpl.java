package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
        List<CourseCategoryTreeDto> result = new ArrayList<>();
        Map<String, CourseCategoryTreeDto> nodeMap = new HashMap<>(32);
        categoryTreeDtos.forEach(item -> {
            if (item.getParentid().equals(id)) {
                nodeMap.put(item.getId(), item);
                result.add(item);
            }
            String parentId = item.getParentid();
            CourseCategoryTreeDto parentNode = nodeMap.get(parentId);
            if (parentNode != null) {
                List<CourseCategoryTreeDto> childrenTreeNodes = parentNode.getChildrenTreeNodes();
                if (childrenTreeNodes == null) {
                    parentNode.setChildrenTreeNodes(new ArrayList<>());
                }
                parentNode.getChildrenTreeNodes().add(item);
            }
        });
        return result;
    }
}
