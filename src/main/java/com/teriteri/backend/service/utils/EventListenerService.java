package com.teriteri.backend.service.utils;

import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

@Service
public class EventListenerService {

    @Value("${directory.chunk}")
    private String CHUNK_DIRECTORY;   // 分片存储目录

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 轮询用户登录状态，将登录过期但还在在线集合的用户移出集合
     */
    @Scheduled(fixedDelay = 1000 * 60 * 10) // 10分钟轮询一次，记得启动类加上 @EnableScheduling 注解以启动任务调度功能
    public void updateLoginMember() {
        Set<Object> lm = redisUtil.getMembers("login_member");
//        System.out.println(lm);
        for (Object id: lm) {
            if (!redisUtil.isExist("security:user:" + id)) {
                redisUtil.delMember("login_member", id);
            }
        }
    }

    /**
     * 每天4点删除三天前未使用的分片文件
     * @throws IOException
     */
    @Scheduled(cron = "0 0 4 * * ?")  // 每天4点0分0秒触发任务 // cron表达式格式：{秒数} {分钟} {小时} {日期} {月份} {星期} {年份(可为空)}
    public void deleteChunks() throws IOException {
        // 获取分片文件的存储目录
        File chunkDir = new File(CHUNK_DIRECTORY);
        // 获取所有分片文件
        File[] chunkFiles = chunkDir.listFiles();
        if (chunkFiles != null && chunkFiles.length > 0) {
            for (File chunkFile : chunkFiles) {
                Path filePath = chunkFile.toPath();
                BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);   // 读取文件属性
                FileTime createTime = attr.creationTime();  // 文件的创建时间
                Instant instant = createTime.toInstant();
                ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());   // 转换为本地时区时间
                LocalDateTime createDateTime = zonedDateTime.toLocalDateTime();     // 获取文件创建时间
                LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);      // 3天前的时间
                if (createDateTime.isBefore(threeDaysAgo)) {
//                    System.out.println("删除分片文件 " + chunkFile.getName());
                    // 文件创建时间早于三天前，删除分片文件
                    chunkFile.delete();
                }
            }
        }
    }
}
