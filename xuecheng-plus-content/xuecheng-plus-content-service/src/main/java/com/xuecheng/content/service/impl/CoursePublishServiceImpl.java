package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.enums.DictionaryCode;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feign.client.MediaServiceClient;
import com.xuecheng.content.feign.client.SearchServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.*;
import com.xuecheng.messagesdk.config.RabbitMqConfig;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.search.po.CourseIndex;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xuecheng.messagesdk.service.impl.MqMessageServiceImpl.FINISH_STATE;

/**
 * @author july
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    TeachplanService teachplanService;

    @Resource
    CourseTeacherService courseTeacherService;

    @Resource
    CourseMarketService courseMarketService;

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    CoursePublishMapper coursePublishMapper;

    @Resource
    MqMessageService mqMessageService;

    @Resource
    MediaServiceClient mediaServiceClient;

    @Resource
    SearchServiceClient searchServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        List<CourseTeacher> teacherList = courseTeacherService.getCourseTeacherList(courseId);
        return new CoursePreviewDto(courseBaseInfo, teachplanTree, teacherList);
    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        String auditStatus = courseBaseInfo.getAuditStatus();
        if (DictionaryCode.AUDIT_PASS.equals(auditStatus)) {
            XueChengPlusException.cast("该课程审核已通过！");
        }
        if (DictionaryCode.AUDIT_SUBMIT.equals(auditStatus)) {
            XueChengPlusException.cast("已提交审核，请等待审核完成。");
        }
        if (!companyId.equals(courseBaseInfo.getCompanyId())) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        if (StringUtils.isEmpty(courseBaseInfo.getPic())){
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //添加课程营销信息
        CourseMarket courseMarket = courseMarketService.getById(courseId);
        coursePublishPre.setMarket(JSON.toJSONString(courseMarket));
        //添加课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree.size() <= 0) {
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }
        coursePublishPre.setTeachplan(JSON.toJSONString(teachplanTree));
        //添加课程教师信息
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeacherList(courseId);
        if (teachplanTree.size() <= 0) {
            XueChengPlusException.cast("提交失败，还没有添加授课老师");
        }
        coursePublishPre.setTeachers(JSON.toJSONString(courseTeachers));
        //设置预发布记录状态, 已提交
        coursePublishPre.setStatus(DictionaryCode.AUDIT_SUBMIT);
        coursePublishPre.setCompanyId(companyId);
        coursePublishPre.setCreateDate(LocalDateTime.now());
        if (coursePublishPreMapper.selectById(courseId) == null){
            //新增
            coursePublishPreMapper.insert(coursePublishPre);
        } else{
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本信息表
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus(DictionaryCode.AUDIT_SUBMIT);
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengPlusException.cast("请先提交课程审核，审核通过才可以发布");
        }
        //本机构只允许提交本机构的课程
        if (!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        //审核通过方可发布
        if (!DictionaryCode.AUDIT_PASS.equals(coursePublishPre.getStatus())){
            XueChengPlusException.cast("操作失败，需要课程审核通过后才可发布。");
        }

        //保存课程发布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * 保存消息表记录
     *
     * @param courseId 课程id
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage(courseId, RabbitMqConfig.COURSE_STATICS, RabbitMqConfig.ADD_INDEX, null);
        if (mqMessage == null){
            XueChengPlusException.cast(CommonError.UNKNOWN_ERROR);
        }
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        File htmlFile = null;
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            configuration.setDefaultEncoding("utf-8");
            //指定模板文件
            Template template = configuration.getTemplate("course_template.ftl");
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>(8);
            map.put("model", coursePreviewInfo);
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            InputStream inputStream = IOUtils.toInputStream(content);
            htmlFile = File.createTempFile("course",".html");
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = null;
        try {
            course = mediaServiceClient.uploadFile(multipartFile, "course", courseId + ".html");
            if (course == null) {
                XueChengPlusException.cast("远程调用媒资服务上传文件失败");
            }
        } finally {
            file.delete();
        }
    }

    @Override
    public Boolean saveCourseIndex(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        if (!searchServiceClient.add(courseIndex)) {
            XueChengPlusException.cast("添加索引失败");
        }
        return true;
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    /**
     * 保存课程发布信息
     * @param courseId 课程Id
     */
    private void saveCoursePublish(Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengPlusException.cast("课程预发布数据为空");
        }
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus(DictionaryCode.COURSE_SUBMIT);

        if (coursePublishMapper.selectById(courseId) == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus(DictionaryCode.COURSE_SUBMIT);
        courseBaseMapper.updateById(courseBase);
    }


    @RabbitListener(queues = {RabbitMqConfig.COURSE_STATICS_QUEUE_NAME})
    public void courseStaticMessage(Message msg) {
        String id = new String(msg.getBody());
        long courseId = Long.parseLong(id);
        if (FINISH_STATE.equals(mqMessageService.getStageOne(courseId))){
            log.debug("该课程静态化课程信息任务已经完成不再处理,任务id:{}", id);
            return;
        }
        //生成静态化页面
        File file = generateCourseHtml(courseId);
        if (file == null){
            XueChengPlusException.cast("课程静态化异常");
        }
        //上传静态化页面
        uploadCourseHtml(courseId, file);
        mqMessageService.completedStageOne(courseId);
    }

    @RabbitListener(queues = {RabbitMqConfig.ADD_INDEX_QUEUE_NAME})
    public void saveCourseIndexMessage(Message msg) {
        String id = new String(msg.getBody());
        long courseId = Long.parseLong(id);
        if (FINISH_STATE.equals(mqMessageService.getStageTwo(courseId))){
            log.debug("该课程是索引信息已经添加不再处理,任务id:{}", id);
            return;
        }
        saveCourseIndex(courseId);
        mqMessageService.completedStageTwo(courseId);
    }
}
