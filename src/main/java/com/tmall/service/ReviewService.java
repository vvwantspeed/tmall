package com.tmall.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tmall.entity.Review;
import com.tmall.entity.User;
import com.tmall.mapper.ReviewMapper;
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
@CacheConfig(cacheNames = "reviews")
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {
    @Autowired
    ReviewMapper reviewMapper;

    @Autowired
    UserService userService;

    @Cacheable(key = "'reviews-pid-'+ #p0")
    public List<Review> list(int pid) {
        return reviewMapper.selectAllByProductId(pid);
    }

    @Cacheable(key = "'reviews-count-pid-'+ #p0")
    public int getCount(Integer pid) {
        return reviewMapper.countByProductId(pid);
    }

    @CacheEvict(allEntries = true)
    public void add(Review review) {
        reviewMapper.insert(review);
    }

    public void setUser(Review review) {
        User user = userService.getById(review.getUserId());
        user.getAnonymousName();
        review.setUser(user);
    }

    public void setUsers(List<Review> reviews) {
        for (Review review : reviews) {
            setUser(review);
        }
    }
}




