package com.teriteri.backend.service.impl.video;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.User;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.pojo.VideoUploadInfo;
import com.teriteri.backend.service.impl.user.UserDetailsImpl;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${directory.cover}")
    private String COVER_DIRECTORY;   // 投稿封面存储目录
    @Value("${directory.video}")
    private String VIDEO_DIRECTORY;   // 投稿视频存储目录
    @Value("${directory.chunk}")
    private String CHUNK_DIRECTORY;   // 分片存储目录

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取视频下一个还没上传的分片序号
     * @param hash 视频的hash值
     * @return CustomResponse对象
     */
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

    /**
     * 上传单个视频分片
     * @param chunk 分片文件
     * @param hash  视频的hash值
     * @param index 当前分片的序号
     * @return  CustomResponse对象
     * @throws IOException
     */
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

    /**
     * 取消上传并且删除该视频的分片文件
     * @param hash 视频的hash值
     * @return CustomResponse对象
     */
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

    /**
     * 接收前端提供的视频信息，包括封面文件和稿件的其他信息，保存完封面后将信息发送到消息队列，并返回投稿成功响应
     * @param cover 封面图片文件
     * @param videoUploadInfo 存放投稿信息的 VideoUploadInfo 对象
     * @return  CustomResponse对象
     * @throws JsonProcessingException
     */
    @Override
    public CustomResponse addVideo(MultipartFile cover, VideoUploadInfo videoUploadInfo) throws JsonProcessingException {
        Integer loginUserId = currentUser.getUserId();
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
        videoUploadInfo.setUid(loginUserId);

//        mergeChunks(videoUploadInfo);   // 这里先暂时用异步操作测试着先

        // 序列化 videoUploadInfo 对象为 String， 往 rabbitmq 中发送投稿信息
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(videoUploadInfo);
        rabbitTemplate.convertAndSend("direct_upload_exchange", "videoUpload", jsonPayload);

        return new CustomResponse();
    }

    /**
     * 已弃用
     * 合并分片并将投稿信息写入数据库，已更换监听消息队列的 handleMergeChunks 方法代替
     * @param vui 存放投稿信息的 VideoUploadInfo 对象
     */
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
