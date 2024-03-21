package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Video;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class VideoController {
    @Autowired
    private VideoService videoService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 更新视频状态，包括过审、不通过、删除，其中审核相关需要管理员权限，删除可以是管理员或者投稿用户
     * @param vid 视频ID
     * @param status 要修改的状态，1通过 2不通过 3删除
     * @return 无data返回 仅返回响应
     */
    @PostMapping("/video/change/status")
    public CustomResponse updateStatus(@RequestParam("vid") Integer vid,
                                       @RequestParam("status") Integer status) {
        try {
            return videoService.updateVideoStatus(vid, status);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "操作失败", null);
        }
    }

    /**
     * 游客访问时的feed流随机推荐
     * @return  返回11条随机推荐视频
     */
    @GetMapping("/video/random/visitor")
    public CustomResponse randomVideosForVisitor() {
        CustomResponse customResponse = new CustomResponse();
        int count = 11;
        Set<Object> idSet = redisUtil.srandmember("video_status:1", count);
        List<Map<String, Object>> videoList = videoService.getVideosWithDataByIds(idSet, 1, count);
        // 随机打乱列表顺序
        Collections.shuffle(videoList);
        customResponse.setData(videoList);
        return customResponse;
    }

    /**
     * 累加获取更多视频
     * @param vids  曾经查询过的视频id列表，用于去重
     * @return  每次返回新的10条视频，以及其id列表，并标注是否还有更多视频可以获取
     */
    @GetMapping("/video/cumulative/visitor")
    public CustomResponse cumulativeVideosForVisitor(@RequestParam("vids") String vids) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = new HashMap<>();
        List<Integer> vidsList = new ArrayList<>();
        if (vids.trim().length() > 0) {
            vidsList = Arrays.stream(vids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());  // 从字符串切分出id列表
        }
        Set<Object> set = redisUtil.getMembers("video_status:1");
        if (set == null) {
            map.put("videos", new ArrayList<>());
            map.put("vids", new ArrayList<>());
            map.put("more", false);
            customResponse.setData(map);
            return customResponse;
        }
        vidsList.forEach(set::remove);  // 去除已获取的元素
        Set<Object> idSet = new HashSet<>();    // 存放将要返回的id集合
        Random random = new Random();
        // 随机获取10个vid
        for (int i = 0; i < 10 && set.size() > 0; i++) {
            Object[] arr = set.toArray();
            int randomIndex = random.nextInt(set.size());
            idSet.add(arr[randomIndex]);
            set.remove(arr[randomIndex]);   // 查过的元素移除
        }
        List<Map<String, Object>> videoList = videoService.getVideosWithDataByIds(idSet, 1, 10);
        Collections.shuffle(videoList);     // 随机打乱列表顺序
        map.put("videos", videoList);
        map.put("vids", idSet);
        if (set.size() > 0) {
            map.put("more", true);
        } else {
            map.put("more", false);
        }
        customResponse.setData(map);
        return customResponse;
    }

    @GetMapping("/video/getone")
    public CustomResponse getOneVideo(@RequestParam("vid") Integer vid) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = videoService.getVideoWithDataById(vid);
        if (map == null) {
            customResponse.setCode(404);
            customResponse.setMessage("特丽丽没找到个视频QAQ");
            return customResponse;
        }
        Video video = (Video) map.get("video");
        if (video.getStatus() != 1) {
            customResponse.setCode(404);
            customResponse.setMessage("特丽丽没找到个视频QAQ");
            return customResponse;
        }
        customResponse.setData(map);
        return customResponse;
    }

    @GetMapping("/video/user-works-count")
    public CustomResponse getUserWorksCount(@RequestParam("uid") Integer uid) {
        return new CustomResponse(200, "OK", redisUtil.zCard("user_video_upload:" + uid));
    }
}
