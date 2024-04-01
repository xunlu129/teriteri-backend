package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Favorite;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.FavoriteService;
import com.teriteri.backend.service.video.FavoriteVideoService;
import com.teriteri.backend.service.video.UserVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class FavoriteVideoController {
    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FavoriteVideoService favoriteVideoService;

    @Autowired
    private UserVideoService userVideoService;

    /**
     * 获取用户收藏了该视频的收藏夹列表
     * @param vid   视频id
     * @return  收藏了该视频的收藏夹列表
     */
    @GetMapping("/video/collected-fids")
    public CustomResponse getCollectedFids(@RequestParam("vid") Integer vid) {
        Integer uid = currentUser.getUserId();
        Set<Integer> fids = findFidsOfUserFavorites(uid);
        Set<Integer> collectedFids = favoriteVideoService.findFidsOfCollected(vid, fids);
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(collectedFids);
        return customResponse;
    }

    /**
     * 收藏或取消收藏某视频
     * @param vid   视频ID
     * @param addArray  包含需要添加收藏的多个收藏夹ID组成的字符串，形式如 1,12,13,20 不能含有字符"["和"]"
     * @param removeArray   包含需要移出收藏的多个收藏夹ID组成的字符串，形式如 1,12,13,20 不能含有字符"["和"]"
     * @return  无数据返回
     */
    @PostMapping("/video/collect")
    public CustomResponse collectVideo(@RequestParam("vid") Integer vid,
                                       @RequestParam("adds") String[] addArray,
                                       @RequestParam("removes") String[] removeArray) {
        CustomResponse customResponse = new CustomResponse();
        Integer uid = currentUser.getUserId();
        Set<Integer> fids = findFidsOfUserFavorites(uid);
        Set<Integer> addSet = Arrays.stream(addArray).map(Integer::parseInt).collect(Collectors.toSet());
        Set<Integer> removeSet = Arrays.stream(removeArray).map(Integer::parseInt).collect(Collectors.toSet());
        boolean allElementsInFids = fids.containsAll(addSet) && fids.containsAll(removeSet);    // 判断添加或移出的收藏夹是否都属于该用户
        if (!allElementsInFids) {
            customResponse.setCode(403);
            customResponse.setMessage("无权操作该收藏夹");
            return customResponse;
        }
        Set<Integer> collectedFids = favoriteVideoService.findFidsOfCollected(vid, fids);   // 原本该用户已收藏该视频的收藏夹ID集合
        if (addSet.size() > 0) {
            favoriteVideoService.addToFav(uid, vid, addSet);
        }
        if (removeSet.size() > 0) {
            favoriteVideoService.removeFromFav(uid, vid, removeSet);
        }
        boolean isCollect = addSet.size() > 0 && collectedFids.size() == 0;
        boolean isCancel = addSet.size() == 0 && collectedFids.size() > 0 && collectedFids.size() == removeSet.size() && collectedFids.containsAll(removeSet);
        if (isCollect) {
            userVideoService.collectOrCancel(uid, vid, true);
        } else if (isCancel) {
            userVideoService.collectOrCancel(uid, vid, false);
        }
        return customResponse;
    }

    /**
     * 取消单个视频在单个收藏夹的收藏
     * @param vid   视频vid
     * @param fid   收藏夹id
     * @return  响应对象
     */
    @PostMapping("/video/cancel-collect")
    public CustomResponse cancelCollect(@RequestParam("vid") Integer vid, @RequestParam("fid") Integer fid) {
        CustomResponse customResponse = new CustomResponse();
        Integer uid = currentUser.getUserId();
        Set<Integer> fids = findFidsOfUserFavorites(uid);
        Set<Integer> removeSet = new HashSet<>();
        removeSet.add(fid);
        if (!fids.containsAll(removeSet)) {
            customResponse.setCode(403);
            customResponse.setMessage("无权操作该收藏夹");
            return customResponse;
        }
        Set<Integer> collectedFids = favoriteVideoService.findFidsOfCollected(vid, fids);   // 原本该用户已收藏该视频的收藏夹ID集合
        favoriteVideoService.removeFromFav(uid, vid, removeSet);
        // 判断是否是最后一个取消收藏的收藏夹，是就要标记视频为未收藏
        boolean isCancel = collectedFids.size() > 0 && collectedFids.size() == removeSet.size() && collectedFids.containsAll(removeSet);
        if (isCancel) {
            userVideoService.collectOrCancel(uid, vid, false);
        }
        return customResponse;
    }

    /**
     * 提取某用户的全部收藏夹信息的FID整合成集合
     * @param uid   用户ID
     * @return  fid集合
     */
    private Set<Integer> findFidsOfUserFavorites(Integer uid) {
        List<Favorite> list = favoriteService.getFavorites(uid, true);
        if (list == null) return new HashSet<>();
        return list.stream()
                .map(Favorite::getFid)
                .collect(Collectors.toSet());
    }
}
