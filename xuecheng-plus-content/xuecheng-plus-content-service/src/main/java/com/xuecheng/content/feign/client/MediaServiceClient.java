package com.xuecheng.content.feign.client;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feign.fallback.MediaServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 媒资管理服务远程接口
 *
 * @author july
 */
@FeignClient(value = "media-api", configuration = MultipartSupportConfig.class, fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

    /**
     * 远程调用方法上传文件
     *
     * @param upload 带上传的文件
     * @param folder 目录
     * @param objectName 对象名称
     * @return 结果
     */
    @RequestMapping(value = "media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("filedata") MultipartFile upload,
                      @RequestParam(value = "folder", required = false) String folder,
                      @RequestParam(value = "objectName", required = false) String objectName);
}
