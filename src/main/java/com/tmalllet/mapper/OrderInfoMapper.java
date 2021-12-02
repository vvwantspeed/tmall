package com.tmalllet.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.tmalllet.entity.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Entity com.tmalllet.entity.OrderInfo
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    List<OrderInfo> selectAll();

    List<OrderInfo> selectAllByUserIdAndStatusNotOrderById(@Param("userId") Integer userId,
                                                        @Param("status") String status);
}




