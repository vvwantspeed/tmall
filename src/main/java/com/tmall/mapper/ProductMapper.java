package com.tmall.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.tmall.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmall.entity.Product
 */
public interface ProductMapper extends BaseMapper<Product> {
    List<Product> selectAllByCategoryId(@Param("category_id") Integer categoryId);

    List<Product> selectAllByNameLikeLimit(@Param("name") String name,
                                           @Param("limit") Integer limit);
}




