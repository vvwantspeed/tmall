package com.tmall.mapper;
import org.apache.ibatis.annotations.Param;

import com.tmall.entity.Property;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Entity com.tmall.entity.Property
 */
public interface PropertyMapper extends BaseMapper<Property> {
    List<Property> selectAllByCategoryId(@Param("category_id") Integer categoryId);
}




