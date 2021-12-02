package com.tmalllet.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.tmalllet.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmalllet.entity.Product
 */
public interface ProductMapper extends BaseMapper<Product> {
    List<Product> selectAllByCategoryId(@Param("category_id") Integer categoryId);

    List<Product> selectAllByNameLikeLimit(@Param("name") String name,
                                           @Param("limit") Integer limit);

    /**
     * 获取活动商品
     */
    List<Product> selectAllInPromotion();

    void increaseSales(Integer pid, Integer amount);

    int decreaseStock(Integer pid, Integer amount);
}




