package com.xuecheng.media.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author July
 * @description 媒资文件Service
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Resource
    MediaFilesMapper mediaFilesMapper;

    @Resource
    MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucketFiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(queryMediaParamsDto.getFileType())) {
            queryWrapper.eq(MediaFiles::getFileType, queryMediaParamsDto.getFileType());
        }

        if (StringUtils.isNotEmpty(queryMediaParamsDto.getFilename())) {
            queryWrapper.like(MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        }
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
        //生成md5值
        String fileId = DigestUtils.md5Hex(bytes);
        //文件名称
        String filename = uploadFileParamsDto.getFilename();
        if (StringUtils.isEmpty(objectName)) {
            //文件Id+文件格式
            objectName = fileId + filename.substring(filename.lastIndexOf("."));
        }
        if (StringUtils.isEmpty(folder)) {
            //通过日期构造文件存储路径
            folder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd/"));
        } else if (folder.charAt(folder.length() - 1) != '/') {
            folder += '/';
        }
        objectName = folder + objectName;
        //往minio存储文件
        addMediaFilesToMinio(bytes, bucketFiles, objectName);
        //从数据库查询文件
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileId, uploadFileParamsDto, bucketFiles, objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 将文件上传到Minio
     *
     * @param bytes 文件字节数组
     * @param bucket bucket名称
     * @param objectName 对象名称
     */
    private void addMediaFilesToMinio(byte[] bytes, String bucket , String objectName) {
        String extension = "";
        if(objectName.contains(".")){
            //文件扩展名
            extension = objectName.substring(objectName.lastIndexOf("."));
        }
        String contentType = getMimeTypeByExtension(extension);
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket).object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            log.error("上传到minio时发生错误：{}", e.getMessage());
            e.printStackTrace();
            XueChengPlusException.cast("上传到minio时发生错误");
        }
    }

    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto,
                                        String bucket, String objectName){
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            //新增
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            if (mediaFilesMapper.insert(mediaFiles) < 0) {
                XueChengPlusException.cast("保存文件信息失败");
            }
        }
        return mediaFiles;
    }

    /**
     * 获取文件的媒体类型
     * @param extension 文件扩展名
     * @return contentType类名
     */
    private String getMimeTypeByExtension(String extension){
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(StringUtils.isNotEmpty(extension)){
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if(extensionMatch != null){
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }
}
