package com.tmalllet.controller;

import com.github.pagehelper.PageInfo;
import com.tmalllet.entity.Product;
import com.tmalllet.service.CategoryService;
import com.tmalllet.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class ProductController {
    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;

    @GetMapping("/categories/{cid}/products")
    public PageInfo<Product> list(@PathVariable("cid") int cid,
                                  @RequestParam(value = "start", defaultValue = "0") int start,
                                  @RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
        start = start < 1 ? 1 : start;
        return productService.page(start, size, cid);
    }

    @PostMapping("/products")
    public Object add(@RequestBody Product product) throws Exception {
        // TODO
        product.setCategoryId(product.getCategory().getId());
        product.setCreateDate(LocalDateTime.now());
        productService.add(product);
        return product;
    }

    @DeleteMapping("/products/{id}")
    public String delete(@PathVariable("id") int id) throws Exception {
        productService.removeById(id);
        return null;
    }

    @PutMapping("/products")
    public Object update(@RequestBody Product product) throws Exception {
        productService.update(product);
        return product;
    }

    @GetMapping("/products/{id}")
    public Product get(@PathVariable("id") int id) throws Exception {
        Product product = productService.getById(id);
        product.setCategory(categoryService.getById(product.getCategoryId()));
        return product;
    }
}
