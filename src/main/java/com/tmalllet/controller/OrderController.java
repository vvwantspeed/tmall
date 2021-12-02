package com.tmalllet.controller;

import com.github.pagehelper.PageInfo;
import com.tmalllet.entity.OrderInfo;
import com.tmalllet.entity.OrderItem;
import com.tmalllet.entity.Product;
import com.tmalllet.entity.User;
import com.tmalllet.service.*;
import com.tmalllet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class OrderController {
    @Autowired
    OrderInfoService orderInfoService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ProductService productService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    UserService userService;

    @GetMapping("/orders")
    public PageInfo<OrderInfo> list(@RequestParam(value = "start", defaultValue = "0") int start,
                                    @RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
        start = start < 1 ? 1 : start;
        PageInfo<OrderInfo> orders = orderInfoService.page(start, size);
        // List<OrderInfo> ordersList = orders.getList();
        // for (OrderInfo order : ordersList) {
        for (OrderInfo order : orders.getList()) {
            User user = userService.getById(order.getUserId());
            order.setUser(user);

            List<OrderItem> items = orderItemService.listByOrderId(order.getId());
            for (OrderItem item : items) {
                Product product = productService.getById(item.getProductId());
                productService.setFirstProductImage(product);
                item.setProduct(product);
                item.setUser(user);
            }
            order.setOrderItems(items);
        }
        // orders.setList(ordersList);
        return orders;
    }

    @PutMapping("deliveryOrder/{oid}")
    public Object deliveryOrder(@PathVariable int oid) throws IOException {
        OrderInfo o = orderInfoService.getById(oid);
        o.setDeliveryDate(LocalDateTime.now());
        o.setStatus(orderInfoService.waitConfirm);
        orderInfoService.updateById(o);
        return Result.success();
    }
}
