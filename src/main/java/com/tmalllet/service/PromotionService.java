package com.tmalllet.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tmalllet.entity.Product;
import com.tmalllet.entity.Promotion;
import com.tmalllet.entity.User;
import com.tmalllet.mapper.PromotionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromotionService extends ServiceImpl<PromotionMapper, Promotion> {

    @Autowired
    PromotionMapper promotionMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserService userService;

    @Autowired
    ProductService productService;

    /**
     * 秒杀时，生成令牌 token
     * @param userId
     * @param productId
     * @param promotionId
     * @return
     */
    public String generateToken(int userId, int productId, int promotionId) {
        if (userId < 0 || productId < 0 || promotionId < 0) {
            return null;
        }

        // 售罄标识
        if (redisTemplate.hasKey("product:stock:over:" + productId)) {
            return null;
        }

        // 校验用户
        User user = userService.findUserFromCache(userId);
        if (user == null) {
            return null;
        }

        // 校验商品
        Product product = productService.findProductInCache(productId);
        if (product == null) {
            return null;
        }

        // 秒杀大闸，实际上是一个参数，比如说1000，1000就是令牌的数量，抢到一个令牌就是数量减一
        ValueOperations v = redisTemplate.opsForValue();
        if (v.decrement("promotion:gate:" + promotionId, 1) < 0) {
            return null;
        }

        // 生成令牌 token 实际上还是用 uuid 生成
        String key = "promotion:token:" + userId + ":" + productId + ":" + promotionId;
        String token = UUID.randomUUID().toString().replace("-", "");
        // 把这个令牌存到redis缓存里，设置10分钟过期，在后面创建订单的时候要用
        v.set(key, token, 10, TimeUnit.MINUTES);

        return token;
    }


    public Promotion getByProductId(Integer pid) {
        return promotionMapper.getOneByProductId(pid);
    }
}
