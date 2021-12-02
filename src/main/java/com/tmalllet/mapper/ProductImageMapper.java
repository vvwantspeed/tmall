package com.tmalllet.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.tmalllet.entity.ProductImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmalllet.entity.ProductImage
 */
public interface ProductImageMapper extends BaseMapper<ProductImage> {
    List<ProductImage> selectAllByProductIdAndTypeOrderByIdDesc(@Param("productId") Integer productId,
                                                                @Param("type") String type);
}




