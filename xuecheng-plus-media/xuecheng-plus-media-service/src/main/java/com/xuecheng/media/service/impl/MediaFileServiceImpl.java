package com.xuecheng.media.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
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

    @Value("${minio.bucket.videofiles}")
    private String bucketVideo;

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
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(),
                pageParams.getPageNo(), pageParams.getPageSize());
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
        try {
            addMediaFilesToMinio(bytes, bucketFiles, objectName);
        } catch (Exception e) {
            log.error("上传到minio时发生错误：{}", e.getMessage());
            e.printStackTrace();
            XueChengPlusException.cast("上传到minio时发生错误");
        }
        //从数据库查询文件
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileId, uploadFileParamsDto, bucketFiles, objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            return RestResponse.success(checkMinioFile(mediaFiles.getBucket(), mediaFiles.getFilePath()));
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunkIndex;
        return RestResponse.success(checkMinioFile(bucketVideo, chunkFilePath));
    }

    @Override
    public RestResponse<Boolean> uploadChunk(byte[] bytes, String fileMd5, int chunk) {
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        try {
            addMediaFilesToMinio(bytes, bucketVideo, chunkFilePath);
            return RestResponse.success(true);
        } catch (Exception e) {
            log.error("上传分块文件:{},失败:{}", chunkFilePath, e.getMessage());
        }
        return RestResponse.validfail(false,"上传分块失败");
    }

    @Override
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        String filename = uploadFileParamsDto.getFilename();
        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
        String extName = filename.substring(filename.lastIndexOf("."));
        //创建临时文件作为合并文件
        File mergeFile = null;
        try {
            mergeFile = File.createTempFile(fileMd5, extName);
        } catch (IOException e) {
            XueChengPlusException.cast("合并文件过程中创建临时文件出错");
        }
        try {
            //开始合并
            byte[] b = new byte[1024];
            try (RandomAccessFile rafWrite = new RandomAccessFile(mergeFile, "rw")) {
                for (File chunkFile : chunkFiles) {
                    try (FileInputStream chunkFileStream = new FileInputStream(chunkFile)) {
                        int len = - 1;
                        while ((len = chunkFileStream.read(b)) != -1) {
                            rafWrite.write(b, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                XueChengPlusException.cast("合并文件过程中出错");
            }
            log.debug("合并文件完成{}", mergeFile.getAbsolutePath());

            //校验文件
            uploadFileParamsDto.setFileSize(mergeFile.length());
            try (InputStream mergeFileInputStream = new FileInputStream(mergeFile)) {
                //对文件进行校验，通过比较md5值
                String newFileMd5 = DigestUtils.md5Hex(mergeFileInputStream);
                if (!fileMd5.equalsIgnoreCase(newFileMd5)) {
                    XueChengPlusException.cast("合并文件校验失败");
                }
                log.debug("合并文件校验通过{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                XueChengPlusException.cast("合并文件校验异常");
            }

            //将临时文件上传至minio
            String mergeFilePath = getFilePathByMd5(fileMd5, extName);
            try {
                addMediaFilesToMinio(fileToBytes(mergeFile), bucketVideo, mergeFilePath);
                log.debug("合并文件上传Minio完成{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                XueChengPlusException.cast("合并文件时上传文件出错");
            }
            //将文件信息存入数据库
            addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketVideo, mergeFilePath);
        } finally {
            //删除临时文件
            try {
                for (File file : chunkFiles) {
                    file.delete();
                }
                mergeFile.delete();
            } catch (Exception e) {
                log.info("删除临时文件出错: {}", e.getMessage());
            }
        }
        return RestResponse.success();
    }

    @Override
    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    /**
     * 将文件上传到Minio
     *
     * @param bytes 文件字节数组
     * @param bucket bucket名称
     * @param objectName 对象名称
     */
    private void addMediaFilesToMinio(byte[] bytes, String bucket , String objectName) throws Exception{
        String extension = "";
        if(objectName.contains(".")){
            //文件扩展名
            extension = objectName.substring(objectName.lastIndexOf("."));
        }
        String contentType = getMimeTypeByExtension(extension);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucket).object(objectName)
                .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                .contentType(contentType)
                .build();
        minioClient.putObject(putObjectArgs);
    }

    /**
     * 将文件信息保存到数据库
     *
     * @param companyId 机构Id
     * @param fileMd5 文件MD5值
     * @param uploadFileParamsDto 上传文件参数
     * @param bucket 文件保存的桶
     * @param objectName 文件名称
     * @return com.xuecheng.media.model.po.MediaFiles 媒资文件信息
     */
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

    /**
     * 得到分块文件的目录
     * @param fileMd5 文件md5值
     * @return 文件目录
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 获得文件路径
     *
     * @param fileMd5 Md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    private String getFilePathByMd5(String fileMd5, String fileExt){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    /**
     * 查询minio系统中文件是否存在
     *
     * @param bucket 桶
     * @param path 路径
     * @return java.lang.Boolean 文件是否存在 false-不存在, true-存在
     */
    private Boolean checkMinioFile(String bucket, String path) {
        InputStream stream = null;
        try {
            stream = minioClient.getObject(GetObjectArgs.
                    builder().bucket(bucket).object(path).build());
            if (stream != null) {
                return true;
            }
        } catch (Exception e) {
            log.info("该文件不存在或者出现未知错误:{}", e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * 检查所有分块是否上传完毕
     * @param fileMd5 文件MD5值
     * @param chunkTotal 分块总数
     * @return java.io.File 全部分块文件
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        String folderPath = getChunkFileFolderPath(fileMd5);
        File[] files = new File[chunkTotal];
        for (int i = 0; i < chunkTotal; i++) {
            String chunkPath = folderPath + i;
            File chunkFile = null;
            try {
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (IOException e) {
                e.printStackTrace();
                XueChengPlusException.cast("下载分块时创建临时文件出错");
            }
            files[i] = downloadFileFromMinio(chunkFile, bucketVideo, chunkPath);
        }
        return files;
    }

    /**
     * 根据桶和文件路径从minio下载文件
     *
     * @param file 文件
     * @param bucket 桶
     * @param objectName 需下载的对象地址
     * @return java.io.File 下载后的文件
     */
    private File downloadFileFromMinio(File file,String bucket,String objectName) {
        InputStream fileInputStream = null;
        OutputStream fileOutputStream = null;
        try {
            fileInputStream = minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucket).object(objectName).build());
            try {
                fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(fileInputStream, fileOutputStream);
            } catch (IOException e) {
                XueChengPlusException.cast("下载文件" + objectName + "出错");
            }
        } catch (Exception e) {
            XueChengPlusException.cast("文件不存在" + objectName);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 将File转为byte数组
     * @param file 待转换的文件
     * @return 字节数组
     */
    private byte[] fileToBytes(File file) {
        byte[] bytes = null;
        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            bytes = new byte[bufferedInputStream.available()];
            int read = bufferedInputStream.read(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
