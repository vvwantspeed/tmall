package com.tmall.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tmall.entity.OrderInfo;
import com.tmall.entity.OrderItem;
import com.tmall.mapper.OrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
@CacheConfig(cacheNames = "orderItems")
public class OrderItemService extends ServiceImpl<OrderItemMapper, OrderItem> {
    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    ProductService productService;
    @Autowired
    OrderInfoService orderInfoService;

    @Cacheable(key = "'orderItems-oid-'+ #p0")
    public List<OrderItem> listByOrderId(Integer oid) {
        return orderItemMapper.getAllByOrderId(oid);
    }

    // 根据产品ID获取总销量
    @Cacheable(key = "'orderItems-count-pid-'+ #p0")
    public int getSaleCount(Integer pid) {
        List<OrderItem> items = listByProductId(pid);
        int result = 0;
        for (OrderItem item : items) {
            OrderInfo order = orderInfoService.getById(item.getOrderId());
            if (null != order && null != order.getPayDate()) {
                    result += item.getNumber();
            }
        }
        return result;
    }

    @Cacheable(key = "'orderItems-pid-'+ #p0")
    public List<OrderItem> listByProductId(Integer pid) {
        return orderItemMapper.getAllByProductId(pid);
    }

    @Cacheable(key = "'orderItems-uid-'+ #p0")
    public List<OrderItem> listUnBuyByUserId(Integer uid) {
        return orderItemMapper.getAllByUserIdAndOrderIdIsNull(uid);
    }

    @CacheEvict(allEntries = true)
    public void add(OrderItem item) {
        orderItemMapper.insert(item);
    }

    public void setProduct(OrderItem item) {
        item.setProduct(productService.getById(item.getProductId()));
    }
}




