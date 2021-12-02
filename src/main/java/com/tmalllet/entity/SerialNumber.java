package com.tmalllet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName serial_number
 */
@TableName(value ="serial_number")
@Data
public class SerialNumber implements Serializable {
    /**
     * 
     */
    @TableId
    private String name;

    /**
     * 
     */
    private Integer value;

    /**
     * 
     */
    private Integer step;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}