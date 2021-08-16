package com.tmall.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tmall.entity.Category;
import com.tmall.entity.Product;
import com.tmall.entity.ProductImage;
import com.tmall.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
@CacheConfig(cacheNames = "products")
public class ProductService extends ServiceImpl<ProductMapper, Product> {
    @Autowired
    ProductMapper productMapper;

    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    ReviewService reviewService;

    @Autowired
    ESService esService;

    @Cacheable(key = "'products-cid-'+#p0")
    public List<Product> listByCategoryId(Integer cid) {
        return productMapper.selectAllByCategoryId(cid);
    }

    @Cacheable(key = "'products-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
    public PageInfo<Product> page(int start, int size, Integer cid) {
        return PageHelper.startPage(start, size).doSelectPageInfo(() -> listByCategoryId(cid));
    }

    @CacheEvict(allEntries = true)
    public void add(Product product) throws IOException {
        productMapper.insert(product);
        esService.add(product);
    }

    @CacheEvict(allEntries = true)
    public void removeById(Integer id) throws Exception {
        super.removeById(id);
        esService.delete(id);
    }


    @CacheEvict(allEntries = true)
    public void update(Product product) throws Exception {
        super.updateById(product);
        esService.update(product);
    }

    @Cacheable(key = "'products-name-like-'+#p0+'-'+#p1")
    public List<Product> searchByName(String keyword, Integer start, Integer size) throws Exception {
        // return productMapper.selectAllByNameLikeLimit(keyword, limit);
        esService.init();
        return esService.searchByName(keyword, start, size);
    }


    // 为产品设置销量和评论数据
    public void setSaleAndReviewNumber(List<Product> products) {
        for (Product product : products)
            setSaleAndReviewNumber(product);
    }

    public void setSaleAndReviewNumber(Product product) {
        int saleCount = orderItemService.getSaleCount(product.getId());
        product.setSaleCount(saleCount);

        int reviewCount = reviewService.getCount(product.getId());
        product.setReviewCount(reviewCount);
    }

    public void setFirstProductImage(Product product) {
        List<ProductImage> singleImages = productImageService.listSingleProductImages(product.getId());
        if (!singleImages.isEmpty()) {
            product.setFirstProductImage(singleImages.get(0));
        } else {
            product.setFirstProductImage(new ProductImage());
        }
    }

    public void setFirstProductImages(List<Product> products) {
        for (Product product : products)
            setFirstProductImage(product);
    }
}




