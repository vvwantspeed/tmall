package com.tmall.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tmall.entity.User;
import com.tmall.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
*
*/
@Service
@CacheConfig(cacheNames = "users")
public class UserService extends ServiceImpl<UserMapper, User> {
    @Autowired
    UserMapper userMapper;

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
}




