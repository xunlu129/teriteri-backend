package com.teriteri.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teriteri.backend.pojo.MainCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MainCategoryMapper extends BaseMapper<MainCategory> {

    // 关联查询来获取主分类和附分类的数据，整合到一个列表中
    @Select("SELECT mc.mc_id, mc.mc_name, sc.sc_id, sc.sc_name " +
            "FROM main_category mc " +
            "LEFT JOIN sub_category sc ON mc.mc_id = sc.mc_id ")
    List<Map<String, Object>> getAllWithSubCategories();

}
