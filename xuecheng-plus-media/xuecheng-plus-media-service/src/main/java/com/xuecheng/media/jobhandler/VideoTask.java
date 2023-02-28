package com.xuecheng.media.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author july
 */
@Slf4j
@Component
public class VideoTask {

    public static final String SUCCESS = "success";

    @Resource
    MediaFileProcessService mediaFileProcessService;

    @Resource
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        int shardTotal = XxlJobHelper.getShardTotal();
        int shardIndex = XxlJobHelper.getShardIndex();
        List<MediaProcess> mediaProcessList = null;
        int size = 0;
        try {
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex, 2);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条", size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(size, size, 10, TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(), new DefaultThreadFactory("videoTask"));

        mediaProcessList.forEach(mediaProcess -> {
            executor.execute(() -> {
                //桶
                String bucket = mediaProcess.getBucket();
                //存储路径
                String filePath = mediaProcess.getFilePath();
                //原始视频的md5值
                String fileId = mediaProcess.getFileId();
                //原始文件名称
                String filename = mediaProcess.getFilename();
                //要处理的视频文件
                File originalFile = null;
                //处理结束的视频文件
                File mp4File = null;
                try {
                    originalFile = File.createTempFile("original", null);
                    mp4File = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    throw new RuntimeException("处理视频前创建临时文件失败");
                }
                try {
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(),
                            mp4File.getName(), mp4File.getAbsolutePath());
                    String result = mp4VideoUtil.generateMp4();
                    if(!SUCCESS.equals(result)) {
                        //记录错误信息
                        log.error("处理视频{}失败,错误信息:{}", filename, result);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("处理视频文件出错" + filePath);
                }
                try {
                    String objectName = getFilePathByMd5(fileId);
                    mediaFileService.addMediaFilesToMinio(mp4File.getAbsolutePath(), bucket, objectName);
                } catch (Exception e) {
                    log.error("上传视频失败,视频地址:{},错误信息:{}", bucket + getFilePathByMd5(fileId), e.getMessage());
                }

            });
        });
    }
    private String getFilePathByMd5(String fileMd5){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + ".mp4";
    }
}
