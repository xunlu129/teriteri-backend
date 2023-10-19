package com.teriteri.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.VideoUploadInfo;
import com.teriteri.backend.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class VideoController {
    @Autowired
    private VideoService videoService;

    @GetMapping("/video/ask-chunk")
    public CustomResponse askChunk(@RequestParam("hash") String hash) {
        return videoService.askCurrentChunk(hash);
    }

    @PostMapping("/video/upload-chunk")
    public CustomResponse uploadChunk(@RequestParam("chunk") MultipartFile chunk,
                                      @RequestParam("hash") String hash,
                                      @RequestParam("index") Integer index) throws IOException {
        return videoService.uploadChunk(chunk, hash, index);
    }

    @GetMapping("/video/cancel-upload")
    public CustomResponse cancelUpload(@RequestParam("hash") String hash) {
        return videoService.cancelUpload(hash);
    }

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
                                   @RequestParam("descr") String descr) throws IOException {
        VideoUploadInfo videoUploadInfo = new VideoUploadInfo(null, hash, title, type, auth, duration, mcid, scid, tags, descr, null);
        return videoService.addVideo(cover, videoUploadInfo);
    }
}
