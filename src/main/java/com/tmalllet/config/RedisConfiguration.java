package com.tmalllet.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmalllet.common.FastJsonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

//Redis 缓存配置类
@Configuration
// @EnableCaching  // 需要这个注解才能启用注解驱动的缓存管理功能
public class RedisConfiguration {
    @Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		// 将template 泛型设置为 <String, Object>
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 连接工厂，不必修改
        template.setConnectionFactory(redisConnectionFactory);


        /*
         * 序列化设置
         */
        // key serializer
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // value serializer
        FastJsonSerializer fastJsonSerializer = new FastJsonSerializer();
        template.setValueSerializer(fastJsonSerializer);
        template.setHashValueSerializer(fastJsonSerializer);

        template.afterPropertiesSet();

        return template;
	}
}
//     @Bean
//         // 关联redis到注解
//     CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//
//         RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
//
//         // 默认配置，过期时间指定是30分钟
//         RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
//         defaultCacheConfig.entryTtl(Duration.ofMinutes(30));
//
//         // redisExpire1h cache配置，过期时间指定是1小时，缓存key的前缀指定成prefixaaa_（存到redis的key会自动添加这个前缀）
//         RedisCacheConfiguration userCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().
//                 entryTtl(Duration.ofHours(1)).prefixKeysWith("prefixaaa_");
//         Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
//         redisCacheConfigurationMap.put("redisExpire1h", userCacheConfiguration);
//
//         RedisCacheManager cacheManager = new RedisCacheManager(redisCacheWriter, defaultCacheConfig, redisCacheConfigurationMap);
//         return cacheManager;
//     }
// }
