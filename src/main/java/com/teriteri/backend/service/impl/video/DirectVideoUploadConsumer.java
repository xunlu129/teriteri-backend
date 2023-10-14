package com.teriteri.backend.service.impl.video;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.mapper.VideoStatsMapper;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.pojo.VideoStats;
import com.teriteri.backend.pojo.VideoUploadInfo;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RabbitListener(queues = "videoUpload_direct_queue")
public class DirectVideoUploadConsumer {

    @Value("${directory.video}")
    private String VIDEO_DIRECTORY;   // 投稿视频存储目录
    @Value("${directory.chunk}")
    private String CHUNK_DIRECTORY;   // 分片存储目录

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 监听消息队列，获取投稿信息，合并分片文件并把信息写入数据库
     * @param jsonPayload 从 rabbitmq 获取的序列化后的投稿信息
     * @throws JsonProcessingException
     */
    @RabbitHandler
    public void handleMergeChunks(String jsonPayload) throws JsonProcessingException {
        // 使用Jackson库将JSON字符串解析为VideoUploadInfo对象
        ObjectMapper objectMapper = new ObjectMapper();
        VideoUploadInfo vui = objectMapper.readValue(jsonPayload, VideoUploadInfo.class);
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
                        vui.getDuration(),
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
//                long start = System.currentTimeMillis();
//                videoStatsMapper.insert(new VideoStats(video.getVid(),0,0,0,0,0,0,0));
//                redisUtil.setExObjectValue("video:" + video.getVid(), video);
//                redisUtil.addMember("video_status:0", video.getVid());  // 将新视频加入待审核集合
//                redisUtil.setValue("video:" + video.getVid() + ":play", 0);
//                redisUtil.setValue("video:" + video.getVid() + ":danmu", 0);
//                redisUtil.setValue("video:" + video.getVid() + ":good", 0);
//                redisUtil.setValue("video:" + video.getVid() + ":bad", 0);
//                redisUtil.setValue("video:" + video.getVid() + ":coin", 0);
//                redisUtil.setValue("video:" + video.getVid() + ":collect", 0);
//                redisUtil.setValue("video:" + video.getVid() + ":share", 0);
//                long end = System.currentTimeMillis();
//                System.out.println("串行耗时：" + (end-start));

                // 使用多线程并行速度提升50%，尽管串行耗时只有122ms，并行耗时60ms
//                long start = System.currentTimeMillis();
                // 提交每个操作以供并行执行
                Future<?>[] futures = new Future<?>[9];
                futures[0] = CompletableFuture.runAsync(() -> videoStatsMapper.insert(new VideoStats(video.getVid(),0,0,0,0,0,0,0)), taskExecutor);
                futures[1] = CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("video:" + video.getVid(), video), taskExecutor);
                futures[2] = CompletableFuture.runAsync(() -> redisUtil.addMember("video_status:0", video.getVid()), taskExecutor);
                futures[3] = CompletableFuture.runAsync(() -> redisUtil.setValue("video:" + video.getVid() + ":play", 0), taskExecutor);
                futures[4] = CompletableFuture.runAsync(() -> redisUtil.setValue("video:" + video.getVid() + ":danmu", 0), taskExecutor);
                futures[5] = CompletableFuture.runAsync(() -> redisUtil.setValue("video:" + video.getVid() + ":good", 0), taskExecutor);
                futures[6] = CompletableFuture.runAsync(() -> redisUtil.setValue("video:" + video.getVid() + ":bad", 0), taskExecutor);
                futures[7] = CompletableFuture.runAsync(() -> redisUtil.setValue("video:" + video.getVid() + ":coin", 0), taskExecutor);
                futures[8] = CompletableFuture.runAsync(() -> redisUtil.setValue("video:" + video.getVid() + ":collect", 0), taskExecutor);
                // 等待所有操作完成
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        // 处理异常
                    }
                }
//                long end = System.currentTimeMillis();
//                System.out.println("并行耗时：" + (end-start));

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
