package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.messagesdk.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author july
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    private static final String PROCESS_FINISH = "2";
    private static final String PROCESS_FAIL = "3";
    private static final String SUCCESS = "success";

    @Resource
    MediaProcessMapper mediaProcessMapper;

    @Resource
    MediaFilesMapper mediaFilesMapper;

    @Resource
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Resource
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegPath;

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        //任务不存在
        if (mediaProcess == null) {
            return;
        }
        LambdaQueryWrapper<MediaProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MediaProcess::getId, taskId);
        //处理失败
        if (PROCESS_FAIL.equals(status)) {
            MediaProcess newMediaProcess = new MediaProcess();
            newMediaProcess.setStatus(PROCESS_FAIL);
            newMediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.update(mediaProcess, queryWrapper);
            return;
        }
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles != null) {
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        mediaProcess.setUrl(url);
        mediaProcess.setStatus(PROCESS_FINISH);
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        //添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //删除mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());
    }

    @RabbitListener(queues = {RabbitMqConfig.VIDEO_PROCESS_QUEUE})
    public void videoProcessMessage(Message msg) {
        File originFile = null;
        File mp4File = null;
        try {
            log.info("收到视频处理消息：{}", msg);

            String id = new String(msg.getBody());
            MediaProcess mediaProcess = mediaProcessMapper.selectById(id);
            String originPath = mediaProcess.getFilePath();
            String bucket = mediaProcess.getBucket();
            String fileId = mediaProcess.getFileId();
            //处理结束的视频文件
            originFile = new File(originPath);
            String result = "";
            try {
                mp4File = File.createTempFile("mp4", ".mp4");
            } catch (IOException e) {
                log.error("处理视频前创建临时文件失败");
                throw new RuntimeException(e);
            }
            try {
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, originPath, mp4File.getName(), mp4File.getAbsolutePath());
                result = videoUtil.generateMp4();
            } catch (Exception e) {
                log.error("处理视频文件:{},出错:{}", originPath, e.getMessage());
                throw new RuntimeException(e);
            }
            if (!SUCCESS.equals(result)) {
                //记录错误信息
                log.error("处理视频失败,错误信息:{}", result);
                saveProcessFinishStatus(mediaProcess.getId(), PROCESS_FAIL, fileId, null, result);
                throw new RuntimeException(result);
            }
            String objectName = getMp4FilePath(fileId);
            try {
                mediaFileService.addMediaFilesToMinio(mp4File.getAbsolutePath(), bucket, objectName);
            } catch (Exception e) {
                log.error("上传视频失败,视频地址:{},错误信息:{}", objectName, e.getMessage());
                throw new RuntimeException(e);
            }
            String url = "/" + bucket + "/" + objectName;
            saveProcessFinishStatus(mediaProcess.getId(), PROCESS_FINISH, fileId, url, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                originFile.delete();
                mp4File.delete();
            } catch (Exception e) {
                log.info("删除临时文件出错: {}", e.getMessage());
            }
        }
    }

    private String getMp4FilePath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + ".mp4";
    }
}
