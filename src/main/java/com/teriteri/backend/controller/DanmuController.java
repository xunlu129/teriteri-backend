package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.Danmu;
import com.teriteri.backend.service.danmu.DanmuService;
import com.teriteri.backend.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
public class DanmuController {
    @Autowired
    private DanmuService danmuService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取弹幕列表
     * @param vid   视频ID
     * @return  CustomResponse对象
     */
    @GetMapping("/danmu-list/{vid}")
    public CustomResponse getDanmuList(@PathVariable("vid") String vid) {
        Set<Object> idset = redisUtil.getMembers("danmu_idset:" + vid);
        List<Danmu> danmuList = danmuService.getDanmuListByIdset(idset);
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(danmuList);
        return customResponse;
    }
}
