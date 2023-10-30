package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.dto.VideoUploadInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoUploadService {
    CustomResponse askCurrentChunk(String hash);

    CustomResponse uploadChunk(MultipartFile chunk, String hash, Integer index) throws IOException;

    CustomResponse cancelUpload(String hash);

    CustomResponse addVideo(MultipartFile cover, VideoUploadInfoDTO videoUploadInfoDTO) throws IOException;
}
