package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.service.search.SearchService;
import com.teriteri.backend.service.user.UserService;
import com.teriteri.backend.service.video.VideoService;
import com.teriteri.backend.utils.ESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    @Autowired
    private ESUtil esUtil;

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    /**
     * 获取热搜词条
     * @return  热搜列表
     */
    @GetMapping("/search/hot/get")
    public CustomResponse getHotSearch() {
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(searchService.getHotSearch());
        return customResponse;
    }

    /**
     * 添加搜索词或者给该搜索词热度加一
     * @param keyword   搜索词
     * @return  返回格式化后的搜索词，有可能为null
     */
    @PostMapping("/search/word/add")
    public CustomResponse addSearchWord(@RequestParam("keyword") String keyword) {
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(searchService.addSearchWord(keyword));
        return customResponse;
    }

    /**
     * 根据输入内容获取相关搜索推荐词
     * @param keyword   关键词
     * @return  包含推荐搜索词的列表
     */
    @GetMapping("/search/word/get")
    public CustomResponse getSearchWord(@RequestParam("keyword") String keyword) throws UnsupportedEncodingException {
        keyword = URLDecoder.decode(keyword, "UTF-8");  // 解码经过url传输的字符串
        CustomResponse customResponse = new CustomResponse();
        if (keyword.trim().length() == 0) {
            customResponse.setData(Collections.emptyList());
        } else {
            customResponse.setData(searchService.getMatchingWord(keyword));
        }
        return customResponse;
    }

    /**
     * 获取各种类型相关数据数量  视频&用户
     * @param keyword   关键词
     * @return  包含视频数量和用户数量的顺序列表
     */
    @GetMapping("/search/count")
    public CustomResponse getCount(@RequestParam("keyword") String keyword) throws UnsupportedEncodingException {
        keyword = URLDecoder.decode(keyword, "UTF-8");  // 解码经过url传输的字符串
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(searchService.getCount(keyword));
        return customResponse;
    }

    /**
     * 搜索相关已过审视频
     * @param keyword   关键词
     * @param page  第几页
     * @return  视频列表
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/search/video/only-pass")
    public CustomResponse getMatchingVideo(@RequestParam("keyword") String keyword, @RequestParam("page") Integer page) throws UnsupportedEncodingException {
        keyword = URLDecoder.decode(keyword, "UTF-8");  // 解码经过url传输的字符串
        CustomResponse customResponse = new CustomResponse();
        List<Integer> vids = esUtil.searchVideosByKeyword(keyword, page, 30, true);
        customResponse.setData(videoService.getVideosWithDataByIdList(vids));
        return customResponse;
    }

    @GetMapping("/search/user")
    public CustomResponse getMatchingUser(@RequestParam("keyword") String keyword, @RequestParam("page") Integer page) throws UnsupportedEncodingException {
        keyword = URLDecoder.decode(keyword, "UTF-8");  // 解码经过url传输的字符串
        CustomResponse customResponse = new CustomResponse();
        List<Integer> uids = esUtil.searchUsersByKeyword(keyword, page, 30);
        customResponse.setData(userService.getUserByIdList(uids));
        return customResponse;
    }
}
