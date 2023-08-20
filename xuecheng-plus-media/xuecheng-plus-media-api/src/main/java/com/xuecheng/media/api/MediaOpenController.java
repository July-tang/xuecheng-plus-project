package com.xuecheng.media.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author july
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Resource
    MediaFileService mediaFileService;

    @Resource
    RedisTemplate<String, String> redisTemplate;

    Random random = new Random();

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {

        if (redisTemplate.opsForValue().get("media" + mediaId) != null) {
            return RestResponse.success(redisTemplate.opsForValue().get("media" + mediaId));
        }
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())) {
            redisTemplate.opsForValue().set("media"  + mediaId, "null", random.nextInt(10) + 10, TimeUnit.SECONDS);
            XueChengPlusException.cast("视频还没有转码处理");
        }
        redisTemplate.opsForValue().set("media" + mediaId, mediaFiles.getUrl(), random.nextInt(40) + 40, TimeUnit.SECONDS);
        return RestResponse.success(mediaFiles.getUrl());
    }
}
