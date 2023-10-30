package com.teriteri.backend.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teriteri.backend.mapper.VideoMapper;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.category.CategoryService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据id分页获取视频信息，包括用户和分区信息
     * @param set   要查询的视频id集合
     * @param index 分页页码
     * @param quantity  每一页查询的数量
     * @return  包含用户信息、分区信息、视频信息的map列表
     */
    @Override
    public List<Map<String, Object>> getVideosWithUserAndCategoryByIds(Set<Object> set, Integer index, Integer quantity) {
        if (index == null) {
            index = 1;
        }
        if (quantity == null) {
            quantity = 10;
        }
        // 检查数据是否足够满足分页查询
        if (set.size() < (index - 1) * quantity + 1) {
            // 如果数据不足以填充当前分页，返回空列表
            return Collections.emptyList();
        }
        // 如果集合不为空，则在数据库主键查询，并且返回没有被删除的视频
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("vid", set);
        queryWrapper.isNull("delete_date");
        // 创建一个分页对象
        Page<Video> page = new Page<>(index, quantity);
        IPage<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        List<Video> videoList = videoPage.getRecords();
        if (videoList == null) return null;

        // 方法1
//        List<Map<String, Object>> mapList = new ArrayList<>();
//        for (Video video : videoList) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("video", video);
//
//            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
//                map.put("user", userService.getUserById(video.getUid()));
//            }, taskExecutor);
//
//            CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
//                map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
//            }, taskExecutor);
//
//            // 等待 userFuture 和 categoryFuture 完成
//            userFuture.join();
//            categoryFuture.join();
//
//            mapList.add(map);
//        }

        // 方法2 并行处理每一个视频，提高效率
        // 先将videoList转换为Stream
        Stream<Video> videoStream = videoList.stream();
        List<Map<String, Object>> mapList = videoStream.parallel() // 利用parallel()并行处理
                .map(video -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("video", video);

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                    }, taskExecutor);

                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);

                    // 使用join()等待userFuture和categoryFuture任务完成
                    userFuture.join();
                    categoryFuture.join();

                    return map;
                })
                .collect(Collectors.toList());

        return mapList;
    }
}
