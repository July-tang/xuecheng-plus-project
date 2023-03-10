package com.xuecheng.search.controller;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.search.po.CourseIndex;
import com.xuecheng.search.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 课程索引接口
 *
 * @author july
 */
@Api(value = "课程信息索引接口", tags = "课程信息索引接口")
@RestController
@RequestMapping("/index")
public class CourseIndexController {

    @Resource
    IndexService indexService;

    @ApiOperation("添加课程索引")
    @PostMapping("/course")
    public Boolean add(@RequestBody CourseIndex courseIndex) {
        Long id = courseIndex.getId();
        if (id == null) {
            XueChengPlusException.cast("课程id为空");
        }
        if (!indexService.addCourseIndex(String.valueOf(id), courseIndex)) {
            XueChengPlusException.cast("添加课程索引失败");
        }
        return true;
    }

    @ApiOperation("更新课程索引")
    @PutMapping("/course")
    public Boolean update(@RequestBody CourseIndex courseIndex) {
        Long id = courseIndex.getId();
        if (id == null) {
            XueChengPlusException.cast("课程id为空");
        }
        if (!indexService.updateCourseIndex(String.valueOf(id), courseIndex)) {
            XueChengPlusException.cast("更新课程索引失败");
        }
        return true;
    }

    @ApiOperation("删除课程索引")
    @DeleteMapping("/course")
    public Boolean delete(@RequestBody CourseIndex courseIndex) {
        Long id = courseIndex.getId();
        if (id == null) {
            XueChengPlusException.cast("课程id为空");
        }
        if (!indexService.deleteCourseIndex(String.valueOf(id))) {
            XueChengPlusException.cast("删除课程索引失败");
        }
        return true;
    }
}
