package com.teriteri.backend.service.video;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.VideoUploadInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface VideoService {
    CustomResponse askCurrentChunk(String hash);

    CustomResponse uploadChunk(MultipartFile chunk, String hash, Integer index) throws IOException;

    CustomResponse cancelUpload(String hash);

    CustomResponse addVideo(MultipartFile cover, VideoUploadInfo videoUploadInfo) throws IOException;
}
