package com.tmalllet.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.tmalllet.entity.PropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmalllet.entity.PropertyValue
 */
public interface PropertyValueMapper extends BaseMapper<PropertyValue> {
    List<PropertyValue> selectAllByProductId(@Param("productId") Integer productId);
}




