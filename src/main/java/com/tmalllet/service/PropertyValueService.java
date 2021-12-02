package com.tmalllet.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tmalllet.entity.Property;
import com.tmalllet.entity.PropertyValue;
import com.tmalllet.mapper.PropertyValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
*
*/
@Service
@CacheConfig(cacheNames = "propertyValues")
public class PropertyValueService extends ServiceImpl<PropertyValueMapper, PropertyValue> {
    @Autowired
    PropertyValueMapper propertyValueMapper;

    @Autowired
    PropertyService propertyService;

    @Cacheable(key = "'propertyValues-pid-'+ #p0")
    public List<PropertyValue> list(Integer pid) {
        return propertyValueMapper.selectAllByProductId(pid);
    }

    public void setProperties(List<PropertyValue> propertyValues) {
        for (PropertyValue propertyValue : propertyValues) {
            setProperty(propertyValue);
        }
    }

    private void setProperty(PropertyValue propertyValue) {
        Property property = propertyService.getById(propertyValue.getPropertyId());
        propertyValue.setProperty(property);
    }
}




