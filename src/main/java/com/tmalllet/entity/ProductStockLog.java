package com.tmalllet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName product_stock_log
 */
@TableName(value ="product_stock_log")
@Data
public class ProductStockLog implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private Integer productId;

    /**
     * 
     */
    private Integer amount;

    /**
     * 0-default; 1-success; 2-failure;
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}