package com.teriteri.backend.service.video;

import com.teriteri.backend.pojo.Favorite;

import java.util.List;
import java.util.Set;

public interface FavoriteService {
    /**
     * 根据是否创建者获取全部可见的收藏夹列表
     * @param uid   用户ID
     * @param isOwner  是否收藏夹拥有者
     * @return  收藏夹列表
     */
    List<Favorite> getFavorites(Integer uid, boolean isOwner);

    /**
     * 创建一个收藏夹
     * @param uid   用户ID
     * @param title 标题
     * @param desc  简介
     * @param visible   是否公开收藏夹 0不公开 1公开
     * @return 返回新创建的这个收藏夹的信息
     */
    Favorite addFavorite(Integer uid, String title, String desc, Integer visible);

    /**
     * 更新收藏夹信息
     * @param fid   收藏夹ID
     * @param uid   用户ID（判断是否创建者）
     * @param title 标题  限80字
     * @param desc  简介  限200字
     * @param visible   是否公开收藏夹 0不公开 1公开
     * @return  返回更新后的收藏夹信息
     */

    Favorite updateFavorite(Integer fid, Integer uid, String title, String desc, Integer visible);

    /**
     * 删除收藏夹，将会把其中的全部视频从收藏夹中移除，并且如果该视频不存在用户的其他收藏夹中，将会变成未收藏状态（该功能略复杂，暂缓开发）
     * @param fid   收藏夹ID
     * @param uid   用户ID（判断是否创建者）
     */
    void delFavorite(Integer fid, Integer uid);

}
