package com.teriteri.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teriteri.backend.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
