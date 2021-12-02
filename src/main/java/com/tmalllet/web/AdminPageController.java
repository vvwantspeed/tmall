package com.tmalllet.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin")
public class AdminPageController {
    @GetMapping({"/", "list_category"})
    public String listCategory() {
        return "admin/list_category";
    }

    @GetMapping("edit_category")
    public String editCategory() {
        return "admin/edit_category";
    }

    @GetMapping("list_property")
    public String listProperty() {
        return "admin/list_property";
    }

    @GetMapping("edit_property")
    public String editProperty() {
        return "admin/edit_property";
    }

    @GetMapping("list_product")
    public String listProduct() {
        return "admin/list_product";
    }

    @GetMapping("edit_product")
    public String editProduct() {
        return "admin/editProduct";
    }

    @GetMapping( "list_productImage")
    public String listProductImage() {
        return "admin/listProductImage";
    }

    @GetMapping("edit_propertyValue")
    public String editPropertyValue() {
        return "admin/editPropertyValue";
    }

    @GetMapping("list_user")
    public String listUser() {
        return "admin/listUser";
    }

    @GetMapping("list_order")
    public String listOrder() {
        return "admin/listOrder";
    }

}
