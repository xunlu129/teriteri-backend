package com.teriteri.backend.service.impl.category;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.CategoryMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.CategoryWrapper;
import com.teriteri.backend.pojo.Category;
import com.teriteri.backend.service.category.CategoryService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取全部分区数据
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse getAll() {
        CustomResponse customResponse = new CustomResponse();
        List<CategoryWrapper> sortedCategories = new ArrayList<>();

        // 尝试从redis中获取数据
        try {
            sortedCategories = redisUtil.getAllList("categoryList", CategoryWrapper.class);
            if (sortedCategories.size() != 0) {
                customResponse.setData(sortedCategories);
                return customResponse;
            }
            log.warn("redis中获取不到分区数据");
        } catch (Exception e) {
            log.error("获取redis分区数据失败");
        }

        // 将分区表一次全部查询出来，再在内存执行处理逻辑，可以减少数据库的IO
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        List<Category> list = categoryMapper.selectList(queryWrapper);

        // 开一个临时整合map
        Map<String, CategoryWrapper> categoryWrapperMap = new HashMap<>();

        for (Category category : list) {
            String mcId = category.getMcId();
            String scId = category.getScId();
            String mcName = category.getMcName();
            String scName = category.getScName();
            String descr = category.getDescr();
            List<String> rcmTag = new ArrayList<>();
            if (category.getRcmTag() != null) {
                String[] strings = category.getRcmTag().split("\n");    // 将每个标签切出来组成列表封装
                rcmTag = Arrays.asList(strings);
            }

            // 先将主分类和空的子分类列表整合到map中
            if (!categoryWrapperMap.containsKey(mcId)) {
                CategoryWrapper categoryWrapper = new CategoryWrapper();
                categoryWrapper.setMcId(mcId);
                categoryWrapper.setMcName(mcName);
                categoryWrapper.setScList(new ArrayList<>());
                categoryWrapperMap.put(mcId, categoryWrapper);
            }

            // 把子分类整合到map的子分类列表里
            Map<String, Object> scMap = new HashMap<>();
            scMap.put("mcId", mcId);
            scMap.put("scId", scId);
            scMap.put("scName", scName);
            scMap.put("descr", descr);
            scMap.put("rcmTag", rcmTag);
            categoryWrapperMap.get(mcId).getScList().add(scMap);

        }

        // 按指定序列排序
        List<String> sortOrder = Arrays.asList("anime", "guochuang", "douga", "game", "kichiku",
                "music", "dance", "cinephile", "ent", "knowledge",
                "tech", "information", "food", "life", "car",
                "fashion", "sports", "animal", "virtual");

        for (String mcId : sortOrder) {
            if (categoryWrapperMap.containsKey(mcId)) {
                sortedCategories.add(categoryWrapperMap.get(mcId));
            }
        }
        // 将分类添加到redis缓存中
        try {
            redisUtil.setAllList("categoryList", sortedCategories);
        } catch (Exception e) {
            log.error("存储redis分类列表失败");
        }
        customResponse.setData(sortedCategories);
        return customResponse;
    }
}