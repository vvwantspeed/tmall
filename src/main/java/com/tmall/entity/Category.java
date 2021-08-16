package com.tmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @TableName category
 */
@TableName(value = "category")
@Data
public class Category implements Serializable {
    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    @TableField(value = "name")
    private String name;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    List<Product> products;
    // 在首页竖状导航的分类名称右边显示推荐产品列表
    // 一个分类会对应多行产品，而一行产品里又有多个产品记录
    @TableField(exist = false)
    List<List<Product>> productsByRow;
}