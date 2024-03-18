package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.dto.VideoUploadInfoDTO;
import com.teriteri.backend.service.video.VideoUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class VideoUploadController {
    @Autowired
    private VideoUploadService videoUploadService;

    /**
     * 查询当前视频准备要上传的分片序号
     * @param hash 视频的hash值
     * @return
     */
    @GetMapping("/video/ask-chunk")
    public CustomResponse askChunk(@RequestParam("hash") String hash) {
        return videoUploadService.askCurrentChunk(hash);
    }

    /**
     * 上传分片
     * @param chunk 分片的blob文件
     * @param hash  视频的hash值
     * @param index 当前分片的序号
     * @return
     * @throws IOException
     */
    @PostMapping("/video/upload-chunk")
    public CustomResponse uploadChunk(@RequestParam("chunk") MultipartFile chunk,
                                      @RequestParam("hash") String hash,
                                      @RequestParam("index") Integer index) throws IOException {
        try {
            return videoUploadService.uploadChunk(chunk, hash, index);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "分片上传失败", null);
        }

    }

    /**
     * 取消上传
     * @param hash 视频的hash值
     * @return
     */
    @GetMapping("/video/cancel-upload")
    public CustomResponse cancelUpload(@RequestParam("hash") String hash) {
        return videoUploadService.cancelUpload(hash);
    }

    /**
     * 添加视频投稿
     * @param cover 封面文件
     * @param hash  视频的hash值
     * @param title 投稿标题
     * @param type  视频类型 1自制 2转载
     * @param auth  作者声明 0不声明 1未经允许禁止转载
     * @param duration 视频总时长
     * @param mcid  主分区ID
     * @param scid  子分区ID
     * @param tags  标签
     * @param descr 简介
     * @return  响应对象
     */
    @PostMapping("/video/add")
    public CustomResponse addVideo(@RequestParam("cover") MultipartFile cover,
                                   @RequestParam("hash") String hash,
                                   @RequestParam("title") String title,
                                   @RequestParam("type") Integer type,
                                   @RequestParam("auth") Integer auth,
                                   @RequestParam("duration") Double duration,
                                   @RequestParam("mcid") String mcid,
                                   @RequestParam("scid") String scid,
                                   @RequestParam("tags") String tags,
                                   @RequestParam("descr") String descr) {
        VideoUploadInfoDTO videoUploadInfoDTO = new VideoUploadInfoDTO(null, hash, title, type, auth, duration, mcid, scid, tags, descr, null);
        try {
            return videoUploadService.addVideo(cover, videoUploadInfoDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "封面上传失败", null);
        }
    }
}
