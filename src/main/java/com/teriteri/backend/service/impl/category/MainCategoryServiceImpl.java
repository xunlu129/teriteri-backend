package com.teriteri.backend.service.impl.category;

import com.teriteri.backend.mapper.MainCategoryMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.MainCategory;
import com.teriteri.backend.pojo.SubCategory;
import com.teriteri.backend.service.category.MainCategoryService;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class MainCategoryServiceImpl implements MainCategoryService {

    @Autowired
    private MainCategoryMapper mainCategoryMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取全部分区（含子分区）数据
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse getAll() {
        CustomResponse customResponse = new CustomResponse();
        List<MainCategory> sortedCategories = new ArrayList<>();

        // 尝试从redis中获取数据
        try {
            sortedCategories = redisUtil.getAllList("categoryList", MainCategory.class);
            if (sortedCategories.size() != 0) {
                customResponse.setData(sortedCategories);
                return customResponse;
            }
            log.warn("redis中获取不到分区数据");
        } catch (Exception e) {
            log.error("获取redis分区数据失败");
        }

        List<Map<String, Object>> allCatMapList = mainCategoryMapper.getAllWithSubCategories();
//        System.out.println(resultMapList);    // [{sc_name=动物综合, mc_name=动物圈, sc_id=animal_composite, mc_id=animal}]

        // 开一个临时整合map
        Map<String, MainCategory> mainCategoryMap = new HashMap<>();

        for (Map<String, Object> resultMap : allCatMapList) {
            String mcId = (String) resultMap.get("mc_id");
            String mcName = (String) resultMap.get("mc_name");
            String scId = (String) resultMap.get("sc_id");
            String scName = (String) resultMap.get("sc_name");

            // 先将主分类和空的子分类列表整合到map中
            if (!mainCategoryMap.containsKey(mcId)) {
                MainCategory mainCategory = new MainCategory();
                mainCategory.setMcId(mcId);
                mainCategory.setMcName(mcName);
                mainCategory.setScList(new ArrayList<>());
                mainCategoryMap.put(mcId, mainCategory);
            }

            // 如果子分类存在，再把子分类整合到map的子分类列表里
            if (scId != null && scName != null) {
                SubCategory subCategory = new SubCategory();
                subCategory.setMcId(mcId);
                subCategory.setScId(scId);
                subCategory.setScName(scName);
                mainCategoryMap.get(mcId).getScList().add(subCategory);
            }
        }

        // 按指定序列排序
        List<String> sortOrder = Arrays.asList("anime", "movie", "guochuang", "tv", "variety",
                "documentary", "douga", "game", "kichiku", "music",
                "dance", "cinephile", "ent", "knowledge", "tech",
                "information", "food", "life", "car", "fashion",
                "sports", "animal", "virtual");

        for (String mcId : sortOrder) {
            if (mainCategoryMap.containsKey(mcId)) {
                sortedCategories.add(mainCategoryMap.get(mcId));
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