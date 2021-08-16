package com.tmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 
 * @TableName order_item
 */
@TableName(value ="order_item")
@Data
public class OrderItem implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "product_id")
    private Integer productId;

    @TableField(exist = false)
    private Product product;

    /**
     * 
     */
    @TableField(value = "order_id")
    private Integer orderId;

    @TableField(exist = false)
    private OrderInfo orderInfo;

    /**
     * 
     */
    @TableField(value = "user_id")
    private Integer userId;

    @TableField(exist = false)
    private User user;

    /**
     * 单价
     */
    @TableField(value = "price")
    private double price;


    /**
     * 
     */
    @TableField(value = "number")
    private Integer number;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}