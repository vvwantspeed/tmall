package com.tmall.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tmall.entity.Category;
import com.tmall.entity.Product;
import com.tmall.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
@CacheConfig(cacheNames = "categories")
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {
    // @Autowired
    @Resource
    CategoryMapper categoryMapper;

    @Autowired
    ProductService productService;
    @Autowired
    ProductImageService productImageService;

    @Cacheable(key = "'categories-list'")
    public List<Category> list() {
        return categoryMapper.selectList(null);
    }

    @Cacheable(key = "'categories-page-' + #p0 + '-' + #p1")
    public PageInfo<Category> page(int start, int size) {
        return PageHelper.startPage(start, size).doSelectPageInfo(() -> list());
    }

    // test
    @Cacheable(key = "'categories-one-'+ #p0")
    public Category get(int id) {
        Category c = categoryMapper.selectById(id);
        return c;
    }

    @CacheEvict(allEntries = true)
    public void add(Category category) {
        categoryMapper.insert(category);
    }

    // 为分类填充所有的产品集合
    public void setProducts(List<Category> categories) {
        for (Category category : categories) {
            setProduct(category);
        }
    }

    // 为分类设置产品
    public void setProduct(Category category) {
        List<Product> products = productService.listByCategoryId(category.getId());
        productService.setFirstProductImages(products);
        category.setProducts(products);
    }

    // 为分类设置推荐产品集合
    public void setProductsByRow(List<Category> categories) {
        int productNumberEachRow = 8;
        for (Category category : categories) {
            List<Product> products = category.getProducts();
            List<List<Product>> productsByRow = new ArrayList<>();
            for (int i = 0; i < products.size(); i += productNumberEachRow) {
                int size = i + productNumberEachRow;
                size = Math.min(size, products.size());
                List<Product> productsOfEachRow = products.subList(i, size);
                productsByRow.add(productsOfEachRow);
            }
            category.setProductsByRow(productsByRow);
        }
    }
}




