package com.tmalllet.controller;

import com.tmalllet.entity.Product;
import com.tmalllet.entity.PropertyValue;
import com.tmalllet.service.ProductService;
import com.tmalllet.service.PropertyService;
import com.tmalllet.service.PropertyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PropertyValueController {
    @Autowired
    ProductService productService;
    @Autowired
    PropertyService propertyService;
    @Autowired
    PropertyValueService propertyValueService;

    @GetMapping("/products/{pid}/propertyValues")
    public List<PropertyValue> list(@PathVariable("pid") Integer pid) throws Exception {
        List<PropertyValue> values = propertyValueService.list(pid);
        Product product = productService.getById(pid);
        // product.setCategory(categoryService.getById(product.getCategoryId()));

        for (PropertyValue value : values) {
            value.setProperty(propertyService.getById(value.getPropertyId()));
            value.setProduct(product);
        }

        return values;
    }

    @PutMapping("/propertyValues")
    public Object update(@RequestBody PropertyValue value) throws Exception {
        propertyValueService.updateById(value);
        return value;
    }
}
