package com.tmalllet.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tmalllet.entity.Property;
import com.tmalllet.mapper.PropertyMapper;
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
@CacheConfig(cacheNames = "properties")
public class PropertyService extends ServiceImpl<PropertyMapper, Property> {
    @Autowired
    PropertyMapper propertyMapper;

    @Cacheable(key = "'properties-cid-'+ #p0")
    public List<Property> list(Integer cid) {
        return propertyMapper.selectAllByCategoryId(cid);
    }

    @Cacheable(key = "'properties-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public PageInfo<Property> page(int start, int size, Integer cid) {
        return PageHelper.startPage(start, size).doSelectPageInfo(() -> list(cid));
    }

    @CacheEvict(allEntries = true)
    public void add(Property property) {
        propertyMapper.insert(property);
    }
}




