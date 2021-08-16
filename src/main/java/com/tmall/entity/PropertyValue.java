package com.tmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName property_value
 */
@TableName(value ="property_value")
@Data
public class PropertyValue implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 产品id
     */
    @TableField(value = "product_id")
    private Integer productId;

    @TableField(exist = false)
    private Product product;

    /**
     * 属性id
     */
    @TableField(value = "property_id")
    private Integer propertyId;

    @TableField(exist = false)
    private Property property;

    /**
     * 
     */
    @TableField(value = "value")
    private String value;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}