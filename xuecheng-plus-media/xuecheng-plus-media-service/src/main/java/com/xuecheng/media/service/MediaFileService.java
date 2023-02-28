package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * 查询媒资文件列表
     *
     * @param companyId           机构Id
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件通用接口
     *
     * @param companyId           机构Id
     * @param uploadFileParamsDto 上传文件信息
     * @param bytes               上传文件的字节数组
     * @param folder              文件目录
     * @param objectName          文件名
     * @return com.xuecheng.media.model.dto.UploadFileResultDto 上传文件响应dto
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5值
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);


    /**
     * 上传分块
     *
     * @param bytes   文件字节
     * @param fileMd5 文件md5
     * @param chunk   分块序号
     * @return com.xuecheng.base.model.RestResponse 结果响应
     */
    RestResponse<Boolean> uploadChunk(byte[] bytes, String fileMd5, int chunk);

    /**
     * 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse 结果响应
     */
    RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /**
     * 根据id获取媒资文件信息
     *
     * @param mediaId 媒资文件id
     * @return 媒资文件信息
     */
    MediaFiles getFileById(String mediaId);

    /**
     * 将文件上传到Minio(大文件)
     *
     * @param filePath 文件路径
     * @param bucket bucket名称
     * @param objectName 对象名称
     */
    void addMediaFilesToMinio(String filePath, String bucket, String objectName);
}
