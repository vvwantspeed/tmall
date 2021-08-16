package com.tmall.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.tmall.entity.Review;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmall.entity.Review
 */
public interface ReviewMapper extends BaseMapper<Review> {
    List<Review> selectAllByProductId(@Param("productId") Integer productId);

    int countByProductId(@Param("productId") Integer productId);
}




