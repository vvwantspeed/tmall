package com.tmall.mapper;
import java.util.List;

import com.tmall.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmall.entity.Category
 */
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectAll();
}




