package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.utils.CurrentUser;
import com.teriteri.backend.service.video.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 站内用户请求某个用户的收藏夹列表（需要jwt鉴权）
     * @param uid   被查看的用户ID
     * @return  包含收藏夹列表的响应对象
     */
    @GetMapping("/favorite/get-all/user")
    public CustomResponse getAllFavoritiesForUser(@RequestParam("uid") Integer uid) {
        Integer loginUid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        if (Objects.equals(loginUid, uid)) {
            customResponse.setData(favoriteService.getFavorites(uid, true));
        } else {
            customResponse.setData(favoriteService.getFavorites(uid, false));
        }
        return customResponse;
    }

    /**
     * 游客请求某个用户的收藏夹列表（不需要jwt鉴权）
     * @param uid   被查看的用户ID
     * @return  包含收藏夹列表的响应对象
     */
    @GetMapping("/favorite/get-all/visitor")
    public CustomResponse getAllFavoritiesForVisitor(@RequestParam("uid") Integer uid) {
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(favoriteService.getFavorites(uid, false));
        return customResponse;
    }

    /**
     * 创建一个新的收藏夹
     * @param title 标题  限80字（需前端做合法判断）
     * @param desc  简介  限200字（需前端做合法判断）
     * @param visible   是否公开 0否 1是
     * @return  包含新创建的收藏夹信息的响应对象
     */
    @PostMapping("/favorite/create")
    public CustomResponse createFavorite(@RequestParam("title") String title,
                                         @RequestParam("desc") String desc,
                                         @RequestParam("visible") Integer visible) {
        Integer uid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(favoriteService.addFavorite(uid, title, desc, visible));
        return customResponse;
    }
}
