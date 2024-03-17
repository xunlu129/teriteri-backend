package com.teriteri.backend.service.search;

import com.teriteri.backend.pojo.HotSearch;

import java.util.List;

public interface SearchService {

    /**
     * 格式化内容后，添加搜索关键词
     * @param text  输入的内容
     * @return  格式化后的内容，可能为null
     */
    String addSearchWord(String text);

    /**
     * 根据输入内容获取推荐搜索词条
     * @param keyword   关键词
     * @return  相关搜索推荐词条 10条
     */
    List<String> getMatchingWord(String keyword);

    /**
     * 获取热搜词条
     * @return  热搜词列表
     */
    List<HotSearch> getHotSearch();

    /**
     * 获取各种类型相关数据数量  视频&用户
     * @param keyword   关键词
     * @return  包含视频数量和用户数量的顺序列表
     */
    List<Long> getCount(String keyword);

}
