package com.tmalllet.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.tmalllet.entity.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmalllet.entity.OrderItem
 */
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    List<OrderItem> getAllByOrderId(@Param("orderId") Integer orderId);

    List<OrderItem> getAllByProductId(@Param("productId") Integer productId);

    List<OrderItem> getAllByUserIdAndOrderIdIsNull(@Param("userId") Integer userId);
}




