package com.tmalllet.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.tmalllet.entity.Promotion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity generator.entity.Promotion
 */
public interface PromotionMapper extends BaseMapper<Promotion> {
    Promotion getOneByProductId(@Param("productId") Integer productId);
}




