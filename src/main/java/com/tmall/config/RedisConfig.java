package com.tmall.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

//Redis 缓存配置类
@Configuration
// @EnableCaching  // 需要这个注解才能启用注解驱动的缓存管理功能
public class RedisConfig {
    @Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		// 将template 泛型设置为 <String, Object>
        RedisTemplate<String, Object> template = new RedisTemplate();
        // 连接工厂，不必修改
        template.setConnectionFactory(redisConnectionFactory);
        /*
         * 序列化设置
         */
		// Json序列化配置
		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		// om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		// String 的序列化
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key、hash的key 采用 String序列化方式
        // template.setKeySerializer(RedisSerializer.string());
        template.setKeySerializer(stringRedisSerializer);
        // template.setHashKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(stringRedisSerializer);
        // value、hash的value 采用 Jackson 序列化方式
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // template.setValueSerializer(RedisSerializer.json());
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        // template.setHashValueSerializer(RedisSerializer.json());
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
