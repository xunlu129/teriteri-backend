package com.teriteri.backend.service.video;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.dto.VideoUploadInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoUploadService {
    /**
     * 获取视频下一个还没上传的分片序号
     * @param hash 视频的hash值
     * @return CustomResponse对象
     */
    CustomResponse askCurrentChunk(String hash);

    /**
     * 上传单个视频分片，当前上传到阿里云对象存储
     * @param chunk 分片文件
     * @param hash  视频的hash值
     * @param index 当前分片的序号
     * @return  CustomResponse对象
     * @throws IOException
     */
    CustomResponse uploadChunk(MultipartFile chunk, String hash, Integer index) throws IOException;

    /**
     * 取消上传并且删除该视频的分片文件
     * @param hash 视频的hash值
     * @return CustomResponse对象
     */
    CustomResponse cancelUpload(String hash);

    /**
     * 接收前端提供的视频信息，包括封面文件和稿件的其他信息，保存完封面后将信息发送到消息队列，并返回投稿成功响应
     * @param cover 封面图片文件
     * @param videoUploadInfoDTO 存放投稿信息的 VideoUploadInfo 对象
     * @return  CustomResponse对象
     * @throws JsonProcessingException
     */
    CustomResponse addVideo(MultipartFile cover, VideoUploadInfoDTO videoUploadInfoDTO) throws IOException;
}
