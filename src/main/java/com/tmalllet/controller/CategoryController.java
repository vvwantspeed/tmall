package com.tmalllet.controller;

import com.github.pagehelper.PageInfo;
import com.tmalllet.entity.Category;
import com.tmalllet.service.CategoryService;
import com.tmalllet.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("categories")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @GetMapping("getOne/{id}")
    public Category getOne(@PathVariable("id") Integer id) {
        return categoryService.get(id);
    }

    @GetMapping
    public PageInfo<Category> list(@RequestParam(value = "start", defaultValue = "0") int start,
                                   @RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
        start = start < 1 ? 1 : start;
        return categoryService.page(start, size);
    }

    @PostMapping
    public Object add(Category category, MultipartFile image, HttpServletRequest request) throws Exception {
        categoryService.add(category);
        saveOrUpdateImageFile(category, image, request);
        return category;
    }

    private void saveOrUpdateImageFile(Category category, MultipartFile image, HttpServletRequest request)
            throws IOException {
        File imageFolder = new File(request.getServletContext().getRealPath("img/category"));
        File file = new File(imageFolder, category.getId() + ".jpg");
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        image.transferTo(file);
        BufferedImage img = ImageUtil.change2jpg(file);
        ImageIO.write(img, "jpg", file);
    }

    @DeleteMapping("{id}")
    public String delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        categoryService.removeById(id);
        File imageFolder = new File(request.getServletContext().getRealPath("img/category"));
        File file = new File(imageFolder, id + ".jpg");
        file.delete();
        return null;
    }

    @PutMapping("{id}")
    public Object update(Category category, MultipartFile image, HttpServletRequest request) throws Exception {
        categoryService.updateById(category);
        if (image != null) {
            saveOrUpdateImageFile(category, image, request);
        }
        return category;
    }


    @GetMapping("{id}")
    public Category get(@PathVariable("id") Integer id) {
        return categoryService.getById(id);
    }
}
