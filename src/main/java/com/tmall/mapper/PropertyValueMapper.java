package com.tmall.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.tmall.entity.PropertyValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmall.entity.PropertyValue
 */
public interface PropertyValueMapper extends BaseMapper<PropertyValue> {
    List<PropertyValue> selectAllByProductId(@Param("productId") Integer productId);
}




