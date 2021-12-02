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
     * 格式：日期 + 流水
     * 示例：20210123000000000001
     * id允许浪费，但是所有订单共用一个流水号机制
     * 优化：可用雪花算法，基于内存
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderID() {
        StringBuilder sb = new StringBuilder();

        // 拼入日期
        sb.append(new SimpleDateFormat("yyyyMMdd").format(new Date()));

        // 获取流水号
        SerialNumber serial = serialNumberMapper.selectOneByName("order_serial");
        Integer value = serial.getValue();

        // 更新流水号
        serial.setValue(value + serial.getStep());
        serialNumberMapper.updateById(serial);

        // 拼入流水号
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

    // 为传入的订单设置订单项
    public void setOrderItem(OrderInfo order) {
        List<OrderItem> items = orderItemService.listByOrderId(order.getId());
        order.setOrderItems(items);
    }

    /**
     * 异步创建订单
     * rocketmq 事务型消息
     * 第一二步，生产者向 mq 服务器发送 half-msg，接收是否投递成功的返回
     * 接下来是 执行本地事务
     * @param userId
     * @param pid
     * @param amount
     * @param promotionId
     * @return
     */
    public Object createOrderAsync(Integer userId, int pid, int amount, int promotionId) {
        // 售罄标识
        if (redisTemplate.hasKey("product:stock:over:" + pid)) {
            return Result.fail("下单失败！");
        }

        // 生成库存流水
        ProductStockLog productStockLog = productService.createProductStockLog(pid, amount);
        // logger.debug("生成库存流水完成 [" + itemStockLog.getId() + "]");
        if (productStockLog == null) {
            return Result.fail("参数不合法！");
        }

        // 消息体
        JSONObject body = new JSONObject();
        // 消费者 扣减库存 用
        body.put("productId", pid);
        body.put("amount", amount);
        // 回查 检查流水 用
        body.put("productStockLogId", productStockLog.getId());

        // 本地事务参数，创建订单用
        JSONObject arg = new JSONObject();
        arg.put("userId", userId);
        arg.put("productId", pid);
        arg.put("amount", amount);
        arg.put("promotionId", promotionId);
        arg.put("productStockLogId", productStockLog.getId());


        // 主题 tamlllet，tag decrease_stock
        String dest = "tmalllet:decrease_stock";
        Message msg = MessageBuilder.withPayload(body.toString()).build();
        try {
            // logger.debug("尝试投递扣减库存消息 [" + body.toString() + "]");
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(dest, msg, arg);
            // 这里是说异步创建订单失败，实际上是投递消息的第一步就失败了
            if (sendResult.getLocalTransactionState() != LocalTransactionState.COMMIT_MESSAGE) {
                return Result.fail("创建订单失败！");
            }
        } catch (MessagingException e) {
            return Result.fail("创建订单失败！");
        }
        return Result.success();
    }

    /**
     * 事务型消息第三步，本地事务，被listener调用
     * 在缓存里预减库存
     * 在 MySQL里创建订单、更新流水状态为1成功
     * @param userId
     * @param pid
     * @param amount
     * @param promotionId
     * @param productStockLogId
     * @return
     * @throws Exception
     */
    public OrderInfo createOrder(int userId, int pid, int amount, int promotionId, String productStockLogId) throws Exception {
        // 校验参数
        if (amount < 1 || (promotionId <= 0)) {
            throw new Exception("指定的参数不合法！");
        }

        Product product = productService.findProductInCache(pid);
        if (product == null) {
            throw new Exception("指定的商品不存在！");
        }

        // 在缓存里扣减库存
        boolean successful = productService.decreaseStockInCache(pid, amount);
        logger.debug("预扣减库存完成 [" + successful + "]");
        if (!successful) {
            throw new Exception("库存不足！");
        }

        Promotion promotion = promotionService.getById(promotionId);

        // 生成一个订单项OrderItem
        OrderItem item = new OrderItem();
        item.setUserId(userId);
        item.setProductId(pid);
        item.setNumber(amount);
        item.setPrice(promotion.getPromotionPrice());
        orderItemService.add(item);


        // 生成订单
        OrderInfo order = new OrderInfo();
        order.setOrderCode(this.generateOrderID());
        order.setCreateDate(LocalDateTime.now());
        order.setUserId(userId);
        order.setStatus(OrderInfoService.waitPay);
        List<OrderItem> items = new LinkedList<>();
        items.add(item);
        // 把订单加入到数据库，并且遍历订单项集合，设置每个订单项的order，更新到数据库
        this.add(order, items);
        logger.debug("生成订单完成 [" + order.getId() + "]");

        // 更新销量
        JSONObject body = new JSONObject();
        body.put("productId", pid);
        body.put("amount", amount);
        Message msg = MessageBuilder.withPayload(body.toString()).build();
        rocketMQTemplate.asyncSend("tmalllet:increase_sales", msg, new SendCallback() {
            // 普通的异步消息
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.debug("投递增加商品销量消息成功");
            }

            @Override
            public void onException(Throwable e) {
                logger.error("投递增加商品销量消息失败", e);
            }
        }, 60 * 1000);

        // 更新库存流水状态
        // 默认是0，1成功，2失败
        productService.updateProductStockLogStatus(productStockLogId, 1);
        logger.debug("更新流水完成 [" + productStockLogId + "]");

        return order;
    }
}




