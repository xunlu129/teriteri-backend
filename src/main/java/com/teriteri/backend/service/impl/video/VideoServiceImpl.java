package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.category.CategoryService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.service.video.VideoStatsService;
import com.teriteri.backend.utils.OssUtil;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
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
        // 以下耗时测试均是查询数据量为3的记录
//        long start = System.currentTimeMillis();
        List<Video> videoList = new CopyOnWriteArrayList<>();   // 使用线程安全的集合类 CopyOnWriteArrayList 保证多线程处理共享List不会出现并发问题

        // 直接数据库分页查询    （平均耗时 13ms）
        List<Object> idList = new ArrayList<>(set);
        endIndex = Math.min(endIndex, idList.size());
        List<Object> sublist = idList.subList(startIndex, endIndex);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("vid", sublist).isNull("delete_date");
        videoList = videoMapper.selectList(queryWrapper);

        // 多线程查 redis   （这个方法反而更慢了，初始 39ms，后续也平均要 13ms）
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//        for (Object vid : set) {
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                try {
//                    // 先查询 redis
//                    Video video = redisUtil.getObject("video:" + vid, Video.class);
//
//                    if (video == null) {
//                        // redis 查不到再查数据库
//                        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
//                        queryWrapper.eq("vid", vid).isNull("delete_date");
//                        video = videoMapper.selectOne(queryWrapper);
//                        if (video != null) {
//                            // 存在数据就添加到返回列表
//                            Video finalVideo = video;
//                            CompletableFuture.runAsync(() -> {
//                                redisUtil.setExObjectValue("video:" + vid, finalVideo);    // 异步更新到redis
//                            }, taskExecutor);
//                            videoList.add(video);
//                        }
//                    } else {
//                        videoList.add(video);
//                    }
//                } catch (Exception e) {
//                    log.error("多线程查询视频时出错了");
//                    throw e;
//                }
//            }, taskExecutor);
//            futures.add(future); // 将 CompletableFuture 添加到 List
//        }
//        // 等待所有 CompletableFuture 完成
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//        allOf.join();

//        long end = System.currentTimeMillis();
//        System.out.println("查询耗时：" + (end - start));
        if (videoList.isEmpty()) return Collections.emptyList();

        // 封装整合
//        start = System.currentTimeMillis();
        // 方法1   （平均耗时 39ms）
//        List<Map<String, Object>> mapList = new ArrayList<>();
//        for (Video video : videoList) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("video", video);
//
//            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
//                map.put("user", userService.getUserById(video.getUid()));
//            }, taskExecutor);
//
//            CompletableFuture<Void> statsFuture = CompletableFuture.runAsync(() -> {
//                map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
//            }, taskExecutor);
//
//            CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
//                map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
//            }, taskExecutor);
//
//            // 等待 userFuture 和 categoryFuture 完成
//            userFuture.join();
//            statsFuture.join();
//            categoryFuture.join();
//
//            mapList.add(map);
//        }

        // 方法2 并行处理每一个视频，提高效率   （初始 32ms，后续平均耗时 13ms）
        // 先将videoList转换为Stream
        Stream<Video> videoStream = videoList.stream();
        List<Map<String, Object>> mapList = videoStream.parallel() // 利用parallel()并行处理
                .map(video -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("video", video);

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                    }, taskExecutor);

                    CompletableFuture<Void> statsFuture = CompletableFuture.runAsync(() -> {
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);

                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);

                    // 使用join()等待全部任务完成
                    userFuture.join();
                    statsFuture.join();
                    categoryFuture.join();

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
            queryWrapper.eq("vid", vid).isNull("delete_date");
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
        }, taskExecutor);
        CompletableFuture<Void> statsFuture = CompletableFuture.runAsync(() -> {
            map.put("stats", videoStatsService.getVideoStatsById(finalVideo.getVid()));
        }, taskExecutor);
        CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
            map.put("category", categoryService.getCategoryById(finalVideo.getMcId(), finalVideo.getScId()));
        }, taskExecutor);
        map.put("video", video);
        // 使用join()等待userFuture和categoryFuture任务完成
        userFuture.join();
        statsFuture.join();
        categoryFuture.join();

        return map;
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
                queryWrapper.eq("vid", vid).isNull("delete_date");
                Video video = videoMapper.selectOne(queryWrapper);
                if (video == null) {
                    customResponse.setCode(404);
                    customResponse.setMessage("视频不见了QAQ");
                    return customResponse;
                }
                Integer lastStatus = video.getStatus();
                video.setStatus(1);     // 更新视频状态通过审核
                int flag = videoMapper.updateById(video);
                if (flag > 0) {
                    // 更新成功
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.addMember("video_status:1", vid);     // 加入新状态
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
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
                queryWrapper.eq("vid", vid).isNull("delete_date");
                Video video = videoMapper.selectOne(queryWrapper);
                if (video == null) {
                    customResponse.setCode(404);
                    customResponse.setMessage("视频不见了QAQ");
                    return customResponse;
                }
                Integer lastStatus = video.getStatus();
                video.setStatus(2);     // 更新视频状态不通过
                int flag = videoMapper.updateById(video);
                if (flag > 0) {
                    // 更新成功
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.addMember("video_status:2", vid);     // 加入新状态
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
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
            queryWrapper.eq("vid", vid).isNull("delete_date");
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
                video.setStatus(3);     // 更新视频状态已删除
                Date now = new Date();
                video.setDeleteDate(now);
                int flag = videoMapper.updateById(video);
                if (flag > 0) {
                    // 更新成功
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
                    // 搞个异步线程去删除OSS的源文件
                    CompletableFuture.runAsync(() -> {
                        ossUtil.deleteFiles(videoPrefix);
                    }, taskExecutor);
                    CompletableFuture.runAsync(() -> {
                        ossUtil.deleteFiles(coverPrefix);
                    }, taskExecutor);
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
