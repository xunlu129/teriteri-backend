package com.teriteri.backend.service.impl.video;

import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.pojo.VideoUploadInfo;
import com.teriteri.backend.service.impl.UserDetailsImpl;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {

    private final String COVER_DIRECTORY = "public/img/cover/";   // 投稿封面存储目录
    private final String VIDEO_DIRECTORY = "public/video/";   // 投稿视频存储目录
    private final String CHUNK_DIRECTORY = "public/chunk/";   // 分片存储目录

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public CustomResponse askCurrentChunk(String hash) {
        // 获取分片文件的存储目录
        File chunkDir = new File(CHUNK_DIRECTORY);
        // 获取存储在目录中的所有分片文件
        File[] chunkFiles = chunkDir.listFiles((dir, name) -> name.startsWith(hash + "-"));
        // 返回还没上传的分片序号
        CustomResponse customResponse = new CustomResponse();
        if (chunkFiles == null) {
            customResponse.setData(0);
        } else {
            customResponse.setData(chunkFiles.length);
        }
        return customResponse;
    }

    @Override
    public CustomResponse uploadChunk(MultipartFile chunk, String hash, Integer index) throws IOException {
        CustomResponse customResponse = new CustomResponse();
        // 构建分片文件名
        String chunkFileName = hash + "-" + index;
        // 构建分片文件的完整路径
        String chunkFilePath = Paths.get(CHUNK_DIRECTORY, chunkFileName).toString();
        // 检查是否已经存在相同的分片文件
        File chunkFile = new File(chunkFilePath);
        if (chunkFile.exists()) {
            log.warn("分片 " + chunkFilePath + " 已存在");
            customResponse.setCode(500);
            customResponse.setMessage("已存在分片文件");
            return customResponse;
        }
        // 保存分片文件到指定目录
        chunk.transferTo(chunkFile);
        // 返回成功响应
        return customResponse;
    }

    @Override
    public CustomResponse cancelUpload(String hash) {
        // 获取分片文件的存储目录
        File chunkDir = new File(CHUNK_DIRECTORY);
        // 获取存储在目录中的所有分片文件
        File[] chunkFiles = chunkDir.listFiles((dir, name) -> name.startsWith(hash + "-"));
//        System.out.println("检索到要删除的文件数: " + chunkFiles.length);
        // 删除全部分片文件
        if (chunkFiles != null && chunkFiles.length > 0) {
            for (File chunkFile : chunkFiles) {
                chunkFile.delete(); // 删除分片文件
            }
        }
//        System.out.println("删除分片完成");
        // 不管删没删成功 返回成功响应
        return new CustomResponse();
    }


    @Override
    public CustomResponse addVideo(MultipartFile cover, VideoUploadInfo videoUploadInfo) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User suser = loginUser.getUser();
        // 值的判定 虽然前端会判 防止非法请求 不过数据库也写不进去 但会影响封面保存
        if (videoUploadInfo.getTitle().trim().length() == 0) {
            return new CustomResponse(500, "标题不能为空", null);
        }
        if (videoUploadInfo.getTitle().length() > 80) {
            return new CustomResponse(500, "标题不能超过80字", null);
        }
        if (videoUploadInfo.getDescr().length() > 2000) {
            return new CustomResponse(500, "简介太长啦", null);
        }
        // 保存封面
        try {
            // 获取当前时间戳
            long timestamp = System.currentTimeMillis();
            String fileName = timestamp + videoUploadInfo.getHash() + ".jpg";
            String path = Paths.get(COVER_DIRECTORY, fileName).toString();
            File file = new File(path);
//            System.out.println(file.getAbsolutePath());
            cover.transferTo(file);
            videoUploadInfo.setCoverUrl(file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 将投稿信息封装发送到消息队列等待监听写库
        videoUploadInfo.setUid(suser.getUid());

        mergeChunks(videoUploadInfo);   // 这里先暂时用异步操作测试着先

        return new CustomResponse();
    }

    @Async
    public void mergeChunks(VideoUploadInfo vui) {
        // 获取分片文件的存储目录
        File chunkDir = new File(CHUNK_DIRECTORY);
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        // 构建最终文件名，将时间戳加到文件名开头
        String finalFileName = timestamp + vui.getHash() + ".mp4";
        // 构建最终文件的完整路径
        String finalFilePath = Paths.get(VIDEO_DIRECTORY, finalFileName).toString();
        // 创建最终文件
        File finalFile = new File(finalFilePath);
        // 获取所有对应分片文件
        File[] chunkFiles = chunkDir.listFiles((dir, name) -> name.startsWith(vui.getHash() + "-"));
        if (chunkFiles != null && chunkFiles.length > 0) {
            // 使用流操作对文件名进行排序，防止出现先合并 10 再合并 2
            List<File> sortedChunkFiles = Arrays.stream(chunkFiles)
                    .sorted(Comparator.comparingInt(file -> Integer.parseInt(file.getName().split("-")[1])))
                    .collect(Collectors.toList());
            try {
//                System.out.println("正在合并视频");
                // 合并分片文件
                for (File chunkFile : sortedChunkFiles) {
                    byte[] chunkBytes = FileUtils.readFileToByteArray(chunkFile);
                    FileUtils.writeByteArrayToFile(finalFile, chunkBytes, true);
                    chunkFile.delete(); // 删除已合并的分片文件
                }
//                System.out.println("合并完成!");
                // 获取绝对路径，仅限本地服务器
                String url = finalFile.getAbsolutePath();
//                System.out.println(url);
//                System.out.println(vui);
                // 存入数据库
                Date now = new Date();
                Video video = new Video(
                    null,
                    vui.getUid(),
                    vui.getTitle(),
                    vui.getType(),
                    vui.getAuth(),
                    vui.getMcId(),
                    vui.getScId(),
                    vui.getTags(),
                    vui.getDescr(),
                    vui.getCoverUrl(),
                    url,
                    0,
                    now,
                    null
                );
                videoMapper.insert(video);

                // 其他逻辑 （发送消息通知写库成功）

            } catch (IOException e) {
                // 处理合并失败的情况 重新入队等
                log.error("合并视频失败");
            }
        } else {
            // 没有找到分片文件 发通知用户投稿失败
            log.error("未找到分片文件 " + vui.getHash());
        }
    }
}
