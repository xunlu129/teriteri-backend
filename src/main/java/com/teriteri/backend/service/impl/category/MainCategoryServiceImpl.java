package com.teriteri.backend.service.impl.category;

import com.teriteri.backend.mapper.MainCategoryMapper;
import com.teriteri.backend.pojo.CustomResponse;
import com.teriteri.backend.pojo.MainCategory;
import com.teriteri.backend.pojo.SubCategory;
import com.teriteri.backend.service.category.MainCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MainCategoryServiceImpl implements MainCategoryService {

    @Autowired
    private MainCategoryMapper mainCategoryMapper;

    @Override
    public CustomResponse getAll() {
        List<Map<String, Object>> resultMapList = mainCategoryMapper.getAllWithSubCategories();
        Map<String, MainCategory> mainCategoryMap = new HashMap<>();

        for (Map<String, Object> resultMap : resultMapList) {
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

        List<MainCategory> sortedMainCategories = new ArrayList<>();
        for (String mcId : sortOrder) {
            if (mainCategoryMap.containsKey(mcId)) {
                sortedMainCategories.add(mainCategoryMap.get(mcId));
            }
        }

        // 封装到返回对象
        CustomResponse customResponse = new CustomResponse();
//        customResponse.setCode(200);
//        customResponse.setMessage("OK");
        customResponse.setData(sortedMainCategories);

        return customResponse;
    }
}