package com.tmalllet.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tmalllet.entity.User;
import com.tmalllet.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
@CacheConfig(cacheNames = "users")
public class UserService extends ServiceImpl<UserMapper, User> {
    @Autowired
    UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Cacheable(key = "'users-list'")
    public List<User> list() {
        return userMapper.selectAll();
    }

    @Cacheable(key = "'users-page-'+#p0+ '-' + #p1")
    public PageInfo<User> page(int start, int size) {
        return PageHelper.startPage(start, size).doSelectPageInfo(() -> list());
    }

    @CacheEvict(allEntries = true)
    public void add(User user) {
        userMapper.insert(user);
    }

    @Cacheable(key = "'users-one-name-'+ #p0")
    public User getOneByName(String userName) {
        return userMapper.getOneByName(userName);
    }

    public boolean isExist(String name) {
        return userMapper.countByName(name) != 0;
    }

    /**
     * 加了一下 redis 缓存 获取用户
     * @param userId
     * @return
     */
    public User findUserFromCache(int userId) {
        if (userId <= 0) {
            return null;
        }

        User user = null;
        String key = "user:" + userId;

        // redis
        user = (User) redisTemplate.opsForValue().get(key);
        if (user != null) {
            return user;
        }

        // mysql
        user = this.getById(userId);
        if (user != null) {
            // logger.debug("同步缓存 [" + user + "]");
            redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
            return user;
        }

        return null;
    }
}




