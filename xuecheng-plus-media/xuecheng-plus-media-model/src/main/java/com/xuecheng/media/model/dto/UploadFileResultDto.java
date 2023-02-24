package com.xuecheng.media.model.dto;

import com.xuecheng.media.model.po.MediaFiles;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description 上传普通文件成功响应结果
 * @author july
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UploadFileResultDto extends MediaFiles {
}
