package com.tmall.mapper;
import java.util.List;
import com.tmall.entity.Product;
import org.apache.ibatis.annotations.Param;

import com.tmall.entity.ProductImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmall.entity.ProductImage
 */
public interface ProductImageMapper extends BaseMapper<ProductImage> {
    List<ProductImage> selectAllByProductIdAndTypeOrderByIdDesc(@Param("productId") Integer productId,
                                                                @Param("type") String type);
}




