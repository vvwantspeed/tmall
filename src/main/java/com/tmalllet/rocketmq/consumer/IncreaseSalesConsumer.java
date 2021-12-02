package com.tmalllet.rocketmq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.tmalllet.service.ProductService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


// 消费者 消费信息，增加销量
@Service
@RocketMQMessageListener(topic = "tmalllet",
        consumerGroup = "tmalllet_sales", selectorExpression = "increase_sales")
public class IncreaseSalesConsumer implements RocketMQListener<String> {
    private Logger logger = LoggerFactory.getLogger(IncreaseSalesConsumer.class);

    @Autowired
    ProductService productService;

    @Override
    public void onMessage(String message) {
        JSONObject param = JSONObject.parseObject(message);
        int pid = (int) param.get("productId");
        int amount = (int) param.get("amount");

        try {
            productService.increaseSales(pid, amount);
            logger.debug("更新销量完成 [" + pid + "]");
        } catch (Exception e) {
            logger.error("更新销量失败", e);
        }
    }
}
