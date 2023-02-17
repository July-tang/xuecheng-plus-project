package com.xuecheng;

import com.xuecheng.content.XuechengPlusContentServiceApplication;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.po.CourseBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author july
 */
@Slf4j
@SpringBootTest(classes = XuechengPlusContentServiceApplication.class)
public class XuechengPlusContentServiceApplicationTests {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Test
    void contextLoads() {
        CourseBase courseBase = courseBaseMapper.selectById(22);
        log.info("查询到数据：{}", courseBase);
        Assertions.assertNotNull(courseBase);
    }
}
