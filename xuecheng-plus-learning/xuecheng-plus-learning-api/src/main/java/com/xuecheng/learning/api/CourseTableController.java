
package com.xuecheng.learning.api;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.CourseTablesService;
import com.xuecheng.learning.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author july
 */
@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@ResponseBody
@RestController
public class CourseTableController {

    @Resource
    CourseTablesService courseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable Long courseId) {
        String userId = UserUtil.getUserId();
        return courseTablesService.addChooseCourse(userId, courseId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnStatus(@PathVariable Long courseId) {
        //登录用户
        String userId = UserUtil.getUserId();
        return courseTablesService.getLearningStatus(userId, courseId);
    }
}
