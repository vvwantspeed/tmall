package com.tmalllet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 
 * @TableName promotion
 */
@TableName(value ="promotion")
@Data
public class Promotion implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private Date startTime;

    /**
     * 
     */
    private Date endTime;

    /**
     * 
     */
    private Integer productId;

    /**
     * 
     */
    private Double promotionPrice;

    private static final long serialVersionUID = 1L;

    /**
     * 获取活动状态
     *
     * @return -1: 活动未开始, 0: 活动进行中, 1: 活动已结束.
     */
    public int getStatus() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (now.before(startTime)) {
            return -1;
        } else if (now.after(endTime)) {
            return 1;
        } else {
            return 0;
        }
    }
}