package com.tmalllet.mapper;
import java.util.List;

import com.tmalllet.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmalllet.entity.Category
 */
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectAll();
}




