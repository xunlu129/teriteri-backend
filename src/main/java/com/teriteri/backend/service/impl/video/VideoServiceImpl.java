package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.category.CategoryService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.ESUtil;
import com.teriteri.backend.utils.OssUtil;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private ESUtil esUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据id分页获取视频信息，包括用户和分区信息
     * @param set   要查询的视频id集合
     * @param index 分页页码 为空默认是1
     * @param quantity  每一页查询的数量 为空默认是10
     * @return  包含用户信息、分区信息、视频信息的map列表
     */
    @Override
    public List<Map<String, Object>> getVideosWithDataByIds(Set<Object> set, Integer index, Integer quantity) {
        if (index == null) {
            index = 1;
        }
        if (quantity == null) {
            quantity = 10;
        }
        int startIndex = (index - 1) * quantity;
        int endIndex = startIndex + quantity;
        // 检查数据是否足够满足分页查询
        if (startIndex > set.size()) {
            // 如果数据不足以填充当前分页，返回空列表
            return Collections.emptyList();
        }
        List<Video> videoList = new CopyOnWriteArrayList<>();   // 使用线程安全的集合类 CopyOnWriteArrayList 保证多线程处理共享List不会出现并发问题

        // 直接数据库分页查询    （平均耗时 13ms）
        List<Object> idList = new ArrayList<>(set);
        endIndex = Math.min(endIndex, idList.size());
        List<Object> sublist = idList.subList(startIndex, endIndex);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("vid", sublist).ne("status", 3);
        videoList = videoMapper.selectList(queryWrapper);
        if (videoList.isEmpty()) return Collections.emptyList();

        // 并行处理每一个视频，提高效率
        // 先将videoList转换为Stream
        Stream<Video> videoStream = videoList.stream();
        List<Map<String, Object>> mapList = videoStream.parallel() // 利用parallel()并行处理
                .map(video -> {
//                    long start = System.currentTimeMillis();
//                    System.out.println("================ 开始查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + start);
                    Map<String, Object> map = new HashMap<>();
                    map.put("video", video);

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);

                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);

                    // 使用join()等待全部任务完成
                    userFuture.join();
                    categoryFuture.join();
//                    long end = System.currentTimeMillis();
//                    System.out.println("================ 结束查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + end + "   耗时 " + (end - start));

                    return map;
                })
                .collect(Collectors.toList());

//        end = System.currentTimeMillis();
//        System.out.println("封装耗时：" + (end - start));
        return mapList;
    }

    /**
     * 根据vid查询单个视频信息，包含用户信息和分区信息
     * @param vid 视频ID
     * @return 包含用户信息、分区信息、视频信息的map
     */
    @Override
    public Map<String, Object> getVideoWithDataById(Integer vid) {
        Map<String, Object> map = new HashMap<>();
        // 先查询 redis
        Video video = redisUtil.getObject("video:" + vid, Video.class);
        if (video == null) {
            // redis 查不到再查数据库
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("vid", vid).ne("status", 3);
            video = videoMapper.selectOne(queryWrapper);
            if (video != null) {
                Video finalVideo1 = video;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("video:" + vid, finalVideo1);    // 异步更新到redis
                }, taskExecutor);
            } else  {
                return null;
            }
        }

        // 多线程异步并行查询用户信息和分区信息并封装
        Video finalVideo = video;
        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            map.put("user", userService.getUserById(finalVideo.getUid()));
            map.put("stats", videoStatsService.getVideoStatsById(finalVideo.getVid()));
        }, taskExecutor);
        CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
            map.put("category", categoryService.getCategoryById(finalVideo.getMcId(), finalVideo.getScId()));
        }, taskExecutor);
        map.put("video", video);
        // 使用join()等待userFuture和categoryFuture任务完成
        userFuture.join();
        categoryFuture.join();

        return map;
    }

    /**
     * 根据有序vid列表查询视频以及相关信息
     * @param list  vid有序列表
     * @return  有序的视频列表
     */
    @Override
    public List<Map<String, Object>> getVideosWithDataByIdList(List<Integer> list) {
        if (list.isEmpty()) return Collections.emptyList();
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("vid", list).ne("status", 3);
        List<Video> videos = videoMapper.selectList(queryWrapper);
        if (videos.isEmpty()) return Collections.emptyList();
        List<Map<String, Object>> mapList = list.stream().parallel().flatMap(
                vid -> {
                    Map<String, Object> map = new HashMap<>();
                    // 找到videos中为vid的视频
                    Video video = videos.stream()
                            .filter(v -> Objects.equals(v.getVid(), vid))
                            .findFirst()
                            .orElse(null);

                    if (video == null) {
                        return Stream.empty(); // 跳过该项
                    }
                    map.put("video", video);

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);

                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);

                    userFuture.join();
                    categoryFuture.join();

                    return Stream.of(map);
                }
        ).collect(Collectors.toList());
        return mapList;
    }

    /**
     * 更新视频状态，包括过审、不通过、删除，其中审核相关需要管理员权限，删除可以是管理员或者投稿用户
     * @param vid   视频ID
     * @param status 要修改的状态，1通过 2不通过 3删除
     * @return 无data返回，仅返回响应信息
     */
    @Override
    public CustomResponse updateVideoStatus(Integer vid, Integer status) {
        CustomResponse customResponse = new CustomResponse();
        Integer userId = currentUser.getUserId();
        if (status == 1 || status == 2) {
            if (!currentUser.isAdmin()) {
                customResponse.setCode(403);
                customResponse.setMessage("您不是管理员，无权访问");
                return customResponse;
            }
            if (status == 1) {
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("vid", vid).ne("status", 3);
                Video video = videoMapper.selectOne(queryWrapper);
                if (video == null) {
                    customResponse.setCode(404);
                    customResponse.setMessage("视频不见了QAQ");
                    return customResponse;
                }
                Integer lastStatus = video.getStatus();
                video.setStatus(1);
                UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("vid", vid).set("status", 1).set("upload_date", new Date());     // 更新视频状态审核通过
                int flag = videoMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.addMember("video_status:1", vid);     // 加入新状态
                    redisUtil.zset("user_video_upload:" + video.getUid(), video.getVid());
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
                    CompletableFuture.runAsync(() -> esUtil.updateVideo(video), taskExecutor);  // 更新ES视频文档
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            }
            else {
                // 目前逻辑跟上面一样的，但是可能以后要做一些如 记录不通过原因 等操作，所以就分开写了
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("vid", vid).ne("status", 3);
                Video video = videoMapper.selectOne(queryWrapper);
                if (video == null) {
                    customResponse.setCode(404);
                    customResponse.setMessage("视频不见了QAQ");
                    return customResponse;
                }
                Integer lastStatus = video.getStatus();
                video.setStatus(2);
                UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("vid", vid).set("status", 2);     // 更新视频状态审核不通过
                int flag = videoMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.addMember("video_status:2", vid);     // 加入新状态
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
                    CompletableFuture.runAsync(() -> esUtil.updateVideo(video), taskExecutor);  // 更新ES视频文档
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            }
        } else if (status == 3) {
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("vid", vid).ne("status", 3);
            Video video = videoMapper.selectOne(queryWrapper);
            if (video == null) {
                customResponse.setCode(404);
                customResponse.setMessage("视频不见了QAQ");
                return customResponse;
            }
            if (Objects.equals(userId, video.getUid()) || currentUser.isAdmin()) {
                String videoUrl = video.getVideoUrl();
                String videoPrefix = videoUrl.split("aliyuncs.com/")[1];  // OSS视频文件名
                String coverUrl = video.getCoverUrl();
                String coverPrefix = coverUrl.split("aliyuncs.com/")[1];  // OSS封面文件名
                Integer lastStatus = video.getStatus();
                UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("vid", vid).set("status", 3).set("delete_date", new Date());     // 更新视频状态已删除
                int flag = videoMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
                    redisUtil.zsetDelMember("user_video_upload:" + video.getUid(), video.getVid());
                    // 搞个异步线程去删除OSS的源文件和ES上的文档
                    CompletableFuture.runAsync(() -> ossUtil.deleteFiles(videoPrefix), taskExecutor);
                    CompletableFuture.runAsync(() -> ossUtil.deleteFiles(coverPrefix), taskExecutor);
                    CompletableFuture.runAsync(() -> esUtil.deleteVideo(vid), taskExecutor);
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            } else {
                customResponse.setCode(403);
                customResponse.setMessage("您没有权限删除视频");
                return customResponse;
            }
        }
        customResponse.setCode(500);
        customResponse.setMessage("更新状态失败");
        return customResponse;
    }
}
