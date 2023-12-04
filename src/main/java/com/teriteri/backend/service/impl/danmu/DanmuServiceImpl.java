package com.teriteri.backend.service.impl.danmu;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.DanmuMapper;
import com.teriteri.backend.pojo.Danmu;
import com.teriteri.backend.service.danmu.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DanmuServiceImpl implements DanmuService {

    @Autowired
    private DanmuMapper danmuMapper;

    /**
     * 根据弹幕ID集合查询弹幕列表
     * @param idset 弹幕ID集合
     * @return  弹幕列表
     */
    @Override
    public List<Danmu> getDanmuListByIdset(Set<Object> idset) {
        if (idset == null || idset.size() == 0) {
            return null;
        }
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", idset).eq("state", 1);
        return danmuMapper.selectList(queryWrapper);
    }
}
