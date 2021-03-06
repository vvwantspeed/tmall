package com.tmalllet.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tmalllet.entity.*;
import com.tmalllet.mapper.OrderInfoMapper;
import com.tmalllet.mapper.SerialNumberMapper;
import com.tmalllet.util.Result;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
@Service
@CacheConfig(cacheNames = "orders")
public class OrderInfoService extends ServiceImpl<OrderInfoMapper, OrderInfo> {
    Logger logger = LoggerFactory.getLogger(OrderInfoService.class);

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

    @Autowired
    ProductService productService;

    @Autowired
    PromotionService promotionService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Autowired
    SerialNumberMapper serialNumberMapper;

    /**
     * ??????????????? + ??????
     * ?????????20210123000000000001
     * id????????????????????????????????????????????????????????????
     * ??????????????????????????????????????????
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderID() {
        StringBuilder sb = new StringBuilder();

        // ????????????
        sb.append(new SimpleDateFormat("yyyyMMdd").format(new Date()));

        // ???????????????
        SerialNumber serial = serialNumberMapper.selectOneByName("order_serial");
        Integer value = serial.getValue();

        // ???????????????
        serial.setValue(value + serial.getStep());
        serialNumberMapper.updateById(serial);

        // ???????????????
        String prefix = "000000000000".substring(value.toString().length());
        sb.append(prefix).append(value);

        return sb.toString();
    }


    @Cacheable(key = "'orders-list'")
    public List<OrderInfo> list() {
        return orderInfoMapper.selectAll();
    }

    @Cacheable(key = "'orders-page-' + #p0+ '-' + #p1")
    public PageInfo<OrderInfo> page(Integer start, Integer size) {
        return PageHelper.startPage(start, size).doSelectPageInfo(() -> list());
    }

    @CacheEvict(allEntries = true)
    @Transactional(propagation = Propagation.REQUIRED, rollbackForClassName = "Exception")
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

    // ?????????????????????????????????
    public void setOrderItem(OrderInfo order) {
        List<OrderItem> items = orderItemService.listByOrderId(order.getId());
        order.setOrderItems(items);
    }

    /**
     * ??????????????????
     * rocketmq ???????????????
     * ??????????????????????????? mq ??????????????? half-msg????????????????????????????????????
     * ???????????? ??????????????????
     * @param userId
     * @param pid
     * @param amount
     * @param promotionId
     * @return
     */
    public Object createOrderAsync(Integer userId, int pid, int amount, int promotionId) {
        // ????????????
        if (redisTemplate.hasKey("product:stock:over:" + pid)) {
            return Result.fail("???????????????");
        }

        // ??????????????????
        ProductStockLog productStockLog = productService.createProductStockLog(pid, amount);
        // logger.debug("???????????????????????? [" + itemStockLog.getId() + "]");
        if (productStockLog == null) {
            return Result.fail("??????????????????");
        }

        // ?????????
        JSONObject body = new JSONObject();
        // ????????? ???????????? ???
        body.put("productId", pid);
        body.put("amount", amount);
        // ?????? ???????????? ???
        body.put("productStockLogId", productStockLog.getId());

        // ????????????????????????????????????
        JSONObject arg = new JSONObject();
        arg.put("userId", userId);
        arg.put("productId", pid);
        arg.put("amount", amount);
        arg.put("promotionId", promotionId);
        arg.put("productStockLogId", productStockLog.getId());


        // ?????? tamlllet???tag decrease_stock
        String dest = "tmalllet:decrease_stock";
        Message msg = MessageBuilder.withPayload(body.toString()).build();
        try {
            // logger.debug("?????????????????????????????? [" + body.toString() + "]");
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(dest, msg, arg);
            // ???????????????????????????????????????????????????????????????????????????????????????
            if (sendResult.getLocalTransactionState() != LocalTransactionState.COMMIT_MESSAGE) {
                return Result.fail("?????????????????????");
            }
        } catch (MessagingException e) {
            return Result.fail("?????????????????????");
        }
        return Result.success();
    }

    /**
     * ?????????????????????????????????????????????listener??????
     * ????????????????????????
     * ??? MySQL???????????????????????????????????????1??????
     * @param userId
     * @param pid
     * @param amount
     * @param promotionId
     * @param productStockLogId
     * @return
     * @throws Exception
     */
    public OrderInfo createOrder(int userId, int pid, int amount, int promotionId, String productStockLogId) throws Exception {
        // ????????????
        if (amount < 1 || (promotionId <= 0)) {
            throw new Exception("???????????????????????????");
        }

        Product product = productService.findProductInCache(pid);
        if (product == null) {
            throw new Exception("???????????????????????????");
        }

        // ????????????????????????
        boolean successful = productService.decreaseStockInCache(pid, amount);
        logger.debug("????????????????????? [" + successful + "]");
        if (!successful) {
            throw new Exception("???????????????");
        }

        Promotion promotion = promotionService.getById(promotionId);

        // ?????????????????????OrderItem
        OrderItem item = new OrderItem();
        item.setUserId(userId);
        item.setProductId(pid);
        item.setNumber(amount);
        item.setPrice(promotion.getPromotionPrice());
        orderItemService.add(item);


        // ????????????
        OrderInfo order = new OrderInfo();
        order.setOrderCode(this.generateOrderID());
        order.setCreateDate(LocalDateTime.now());
        order.setUserId(userId);
        order.setStatus(OrderInfoService.waitPay);
        List<OrderItem> items = new LinkedList<>();
        items.add(item);
        // ????????????????????????????????????????????????????????????????????????????????????order?????????????????????
        this.add(order, items);
        logger.debug("?????????????????? [" + order.getId() + "]");

        // ????????????
        JSONObject body = new JSONObject();
        body.put("productId", pid);
        body.put("amount", amount);
        Message msg = MessageBuilder.withPayload(body.toString()).build();
        rocketMQTemplate.asyncSend("tmalllet:increase_sales", msg, new SendCallback() {
            // ?????????????????????
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.debug("????????????????????????????????????");
            }

            @Override
            public void onException(Throwable e) {
                logger.error("????????????????????????????????????", e);
            }
        }, 60 * 1000);

        // ????????????????????????
        // ?????????0???1?????????2??????
        productService.updateProductStockLogStatus(productStockLogId, 1);
        logger.debug("?????????????????? [" + productStockLogId + "]");

        return order;
    }
}




