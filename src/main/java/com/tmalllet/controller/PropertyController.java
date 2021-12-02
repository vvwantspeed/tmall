package com.tmalllet.controller;

import com.github.pagehelper.PageInfo;
import com.tmalllet.entity.Property;
import com.tmalllet.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
// @RequestMapping("properties")
@RequestMapping("")
public class PropertyController {
    @Autowired
    PropertyService propertyService;

    @GetMapping("categories/{cid}/properties")
    public PageInfo<Property> list(@PathVariable("cid") Integer cid,
                                   @RequestParam(value = "start", defaultValue = "0") int start,
                                   @RequestParam(value = "size", defaultValue = "5")  int size) throws Exception {
        start = start < 1 ? 1 : start;
        return propertyService.page(start, size, cid);
    }

    @PostMapping("properties")
    public Object add(@RequestBody Property property) {
        // TODO
        // Property(id=0, name=ccccc, category_id=null, category=Category(id=60, name=null))
        property.setCategoryId(property.getCategory().getId());
        propertyService.add(property);
        return property;
    }

    @DeleteMapping("properties/{id}")
    public Object delete(@PathVariable("id") Integer id) {
        propertyService.removeById(id);
        return null;
    }

    @PutMapping("properties")
    public Object update(@RequestBody Property property) {
        propertyService.updateById(property);
        return property;
    }

    @GetMapping("properties/{id}")
    public  Object get(@PathVariable("id") Integer id) {
        return propertyService.getById(id);
    }
}
