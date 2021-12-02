package com.tmalllet.rocketmq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.tmalllet.service.ProductService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * mq server 接收到本地事务完成的commit，将半成品消息放到真正的消息队列里
 * 这里设置消费者监听该消息队列，在 MySQL 里扣减库存
 */
@Service
@RocketMQMessageListener(topic = "tmalllet",
        consumerGroup = "tmalllet_stock", selectorExpression = "decrease_stock")
public class DecreaseStockConsumer implements RocketMQListener<String> {
    Logger logger = LoggerFactory.getLogger(DecreaseStockConsumer.class);

    @Autowired
    ProductService productService;


    @Override
    public void onMessage(String message) {
        JSONObject param = JSONObject.parseObject(message);

        int productId = (int) param.get("productId");
        int amount = (int) param.get("amount");

        try {
            productService.decreaseStock(productId, amount);
            logger.debug("最终扣减库存完成 [" + param.get("itemStockLogId") + "]");
        } catch (Exception e) {
            logger.error("从DB扣减库存失败", e);
        }
    }
}
