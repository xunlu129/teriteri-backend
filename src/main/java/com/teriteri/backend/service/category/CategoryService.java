package com.teriteri.backend.service.category;

import com.teriteri.backend.pojo.Category;
import com.teriteri.backend.pojo.CustomResponse;

public interface CategoryService {
    /**
     * 获取全部分区数据
     * @return CustomResponse对象
     */
    CustomResponse getAll();

    /**
     * 根据id查询对应分区信息
     * @param mcId 主分区ID
     * @param scId 子分区ID
     * @return Category类信息
     */
    Category getCategoryById(String mcId, String scId);
}
