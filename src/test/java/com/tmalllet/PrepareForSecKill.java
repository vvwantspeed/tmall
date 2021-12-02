package com.tmalllet;

import com.tmalllet.entity.Product;
import com.tmalllet.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@SpringBootTest
public class PrepareForSecKill {
    @Autowired
    ProductService productService;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 缓存预热
     * 在 redis 里存商品id和当前库存
     */
    @Test
    public void cacheItemStock() {
        List<Product> list = productService.findProductsOnPromotion();
        for (Product product : list) {
            redisTemplate.opsForValue().set("product:stock:" + product.getId(), product.getStock());
        }
    }

    /**
     * 缓存预热
     * 模拟设置大闸
     * redis 存每个商品对应的活动id，和可以放进来的令牌数量 stock * n
     */
    @Test
    public void initPromotionGate() {
        List<Product> list = productService.findProductsOnPromotion();
        for (Product product : list) {
            redisTemplate.opsForValue().set("promotion:gate:" + product.getPromotion().getId(), product.getStock() * 5);
        }
    }
}
