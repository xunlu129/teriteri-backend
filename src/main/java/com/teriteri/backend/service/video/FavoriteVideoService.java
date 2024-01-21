package com.teriteri.backend.service.video;

import java.util.List;
import java.util.Set;

public interface FavoriteVideoService {
    /**
     * 查询提供的收藏夹ID列表中哪些收藏了指定视频
     * @param vid   视频ID
     * @param fids   收藏夹ID列表
     * @return  已收藏该视频的收藏夹ID集合
     */
    Set<Integer> findFidsOfCollected(Integer vid, Set<Integer> fids);

    /**
     * 将视频添加到多个收藏夹
     * @param vid   视频ID
     * @param fids  需要添加的收藏夹ID集合
     */
    void addToFav(Integer uid, Integer vid, Set<Integer> fids);

    /**
     * 将视频从多个收藏夹中移出
     * @param vid   视频ID
     * @param fids  需要移出的收藏夹ID集合
     */
    void removeFromFav(Integer uid, Integer vid, Set<Integer> fids);
}
