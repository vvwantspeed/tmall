package com.tmalllet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName property
 */
@TableName(value ="property")
@Data
public class Property implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 属性名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 对应分类id
     */
    @TableField(value = "category_id")
    private Integer categoryId;

    @TableField(exist = false)
    private Category category;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}