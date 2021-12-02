package com.tmalllet.rocketmq.producer;

import com.alibaba.fastjson.JSONObject;
import com.tmalllet.entity.OrderInfo;
import com.tmalllet.entity.ProductStockLog;
import com.tmalllet.service.OrderInfoService;
import com.tmalllet.service.ProductService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RocketMQTransactionListener
// 全局的 listener
public class LocalTransactionListenerImpl implements RocketMQLocalTransactionListener {

    private Logger logger = LoggerFactory.getLogger(LocalTransactionListenerImpl.class);

    @Autowired
    private OrderInfoService orderService;

    @Autowired
    ProductService productService;


    /**
     * 事务型消息第3步
     * 生产者要求 MySQL 数据库执行本地事务的
     *
     * @param msg
     * @param arg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            // 获取消息的tag，根据tag执行不同的逻辑
            String tag = msg.getHeaders().get("rocketmq_TAGS").toString();
            if ("decrease_stock".equals(tag)) {
                return this.createOrder(msg, arg);
            } else {
                return RocketMQLocalTransactionState.UNKNOWN;
            }

        } catch (Exception e) {
            logger.error("执行MQ本地事务时发生错误", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 在缓存里预减库存，在MySQL里创建订单、更新流水
     * @param msg
     * @param arg
     * @return
     */
    private RocketMQLocalTransactionState createOrder(Message msg, Object arg) {
        // 从参数里解析消息
        JSONObject param = (JSONObject) arg;
        int userId = (int) param.get("userId");
        int productId = (int) param.get("productId");
        int amount = (int) param.get("amount");
        int promotionId = (int) param.get("promotionId");
        // 流水id
        String productStockLogId = (String) param.get("productStockLogId");

        try {
            // 在缓存里预减库存，在MySQL里创建订单、更新流水
            OrderInfo order = orderService.createOrder(userId, productId, amount, promotionId, productStockLogId);
            logger.debug("本地事务提交完成 [" + order.getId() + "]");
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            logger.error("创建订单失败", e);
            // 更新流水状态为 2 失败
            productService.updateProductStockLogStatus(productStockLogId, 2);
            logger.debug("更新流水完成 [" + productStockLogId + "]");
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 第六步，mq server迟迟拿不到正常流程上的消息，自己去检查
     * @param msg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        try {
            String tag = (String) msg.getHeaders().get("rocketmq_TAGS");
            if ("decrease_stock".equals(tag)) {
                return this.checkStockStatus(msg);
            } else {
                return RocketMQLocalTransactionState.UNKNOWN;
            }
        } catch (Exception e) {
            logger.error("检查MQ本地事务时发生错误", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    private RocketMQLocalTransactionState checkStockStatus(Message msg) {
        JSONObject body = JSONObject.parseObject(new String((byte[]) msg.getPayload()));
        String logId = (String) body.get("productStockLogId");

        ProductStockLog productStockLog = productService.findProductStockLogById(logId);
        logger.debug("检查事务状态完成 [" + productStockLog + "]");
        if (productStockLog == null) {
            return RocketMQLocalTransactionState.ROLLBACK;
        } else if (productStockLog.getStatus() == 0) {
            return RocketMQLocalTransactionState.UNKNOWN;
        } else if (productStockLog.getStatus() == 1) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}
