package com.tmall.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tmall.entity.ProductImage;
import com.tmall.mapper.ProductImageMapper;
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
@CacheConfig(cacheNames = "productImages")
public class ProductImageService extends ServiceImpl<ProductImageMapper, ProductImage> {
    @Autowired
    ProductImageMapper productImageMapper;

    public static final String type_single = "single";
    public static final String type_detail = "detail";

    @Cacheable(key = "'productImages-single-pid-'+ #p0")
    public List<ProductImage> listSingleProductImages(Integer pid) {
        return productImageMapper.selectAllByProductIdAndTypeOrderByIdDesc(pid, type_single);
    }

    @Cacheable(key = "'productImages-detail-pid-'+ #p0")
    public List<ProductImage> listDetailProductImages(Integer pid) {
        return productImageMapper.selectAllByProductIdAndTypeOrderByIdDesc(pid, type_detail);
    }

    @CacheEvict(allEntries = true)
    public void add(ProductImage productImage) {
        productImageMapper.insert(productImage);
    }
}




