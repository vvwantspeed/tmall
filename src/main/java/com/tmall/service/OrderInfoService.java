package com.tmall.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tmall.entity.OrderInfo;
import com.tmall.entity.OrderItem;
import com.tmall.mapper.OrderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 */
@Service
@CacheConfig(cacheNames = "orders")
public class OrderInfoService extends ServiceImpl<OrderInfoMapper, OrderInfo> {
    public static final String waitPay = "waitPay";
    public static final String waitDelivery = "waitDelivery";
    public static final String waitConfirm = "waitConfirm";
    public static final String waitReview = "waitReview";
    public static final String finish = "finish";
    public static final String delete = "delete";

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderItemService orderItemService;


    @Cacheable(key = "'orders-list'")
    public List<OrderInfo> list() {
        return orderInfoMapper.selectAll();
    }

    @Cacheable(key = "'orders-page-' + #p0+ '-' + #p1")
    public PageInfo<OrderInfo> page(Integer start, Integer size) {
        return PageHelper.startPage(start, size).doSelectPageInfo(() -> list());
    }

    // TODO
    @CacheEvict(allEntries = true)
    @Transactional(propagation= Propagation.REQUIRED,rollbackForClassName="Exception")
    public void add(OrderInfo order, List<OrderItem> items) {
        double total = 0;
        int number = 0;
        orderInfoMapper.insert(order);
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            total += item.getNumber() * item.getPrice();
            number += item.getNumber();
            orderItemService.updateById(item);
        }
        order.setTotalPrice(total);
        order.setTotalNumber(number);
        orderInfoMapper.updateById(order);
    }

    @Cacheable(key = "'orders-uid-'+ #p0")
    public List<OrderInfo> listByUserIdWithoutDelete(Integer uid) {
        return orderInfoMapper.selectAllByUserIdAndStatusNotOrderById(uid, delete);
    }

    // 为传入的订单设置订单项
    public void setOrderItem(OrderInfo order) {
        List<OrderItem> items = orderItemService.listByOrderId(order.getId());
        order.setOrderItems(items);
    }
}




