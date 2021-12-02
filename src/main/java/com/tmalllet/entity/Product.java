package com.tmalllet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * @TableName product
 */
@TableName(value = "product")
@Data
public class Product implements Serializable {
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

    /**
     *
     */
    @TableField(value = "subTitle")
    private String subTitle;

    /**
     *
     */
    @TableField(value = "originalPrice")
    private Double originalPrice;

    /**
     *
     */
    @TableField(value = "promotePrice")
    private Double promotePrice;

    /**
     *
     */
    @TableField(value = "stock")
    private Integer stock;

    /**
     * 销量
     */
    @TableField(value = "sales")
    private Integer sales;

    /**
     *
     */
    @TableField(value = "category_id")
    private Integer categoryId;

    @TableField(exist = false)
    private Category category;

    /**
     *
     */
    @TableField(value = "createDate")
    private LocalDateTime createDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private ProductImage firstProductImage;

    // 单个产品图片集合
    @TableField(exist = false)
    private List<ProductImage> productSingleImages;

    // 详情产品图片集合
    @TableField(exist = false)
    private List<ProductImage> productDetailImages;

    // 评价
    @TableField(exist = false)
    private int reviewCount;
    // 销量
    @TableField(exist = false)
    private int saleCount;

    // 是否参加秒杀活动
    @TableField(exist = false)
    private Promotion promotion;
}