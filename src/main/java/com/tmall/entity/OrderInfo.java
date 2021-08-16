package com.tmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.tmall.service.OrderInfoService;
import lombok.Data;

/**
 * @TableName order_info
 */
@TableName(value = "order_info")
@Data
public class OrderInfo implements Serializable {
    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    @TableField(value = "orderCode")
    private String orderCode;

    /**
     *
     */
    @TableField(value = "address")
    private String address;

    /**
     *
     */
    @TableField(value = "post")
    private String post;

    /**
     *
     */
    @TableField(value = "receiver")
    private String receiver;

    /**
     *
     */
    @TableField(value = "mobile")
    private String mobile;

    /**
     *
     */
    @TableField(value = "userMessage")
    private String userMessage;

    /**
     *
     */
    @TableField(value = "createDate")
    private LocalDateTime createDate;

    /**
     *
     */
    @TableField(value = "payDate")
    private LocalDateTime payDate;

    /**
     *
     */
    @TableField(value = "deliveryDate")
    private LocalDateTime deliveryDate;

    /**
     *
     */
    @TableField(value = "confirmDate")
    private LocalDateTime confirmDate;

    /**
     *
     */
    @TableField(value = "user_id")
    private Integer userId;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private List<OrderItem> orderItems;

    /**
     *
     */
    @TableField(value = "status")
    private String status;

    /**
     * 订单总金额
     */
    @TableField(value = "total_price")
    private double totalPrice;

    /**
     * 订单总数量
     */
    @TableField(value = "total_number")
    private int totalNumber;    // 总计数量


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public String getStatusDesc() {
        String desc = "未知";
        switch (status) {
            case OrderInfoService.waitPay:
                desc = "待付款";
                break;
            case OrderInfoService.waitDelivery:
                desc = "待发货";
                break;
            case OrderInfoService.waitConfirm:
                desc = "待收货";
                break;
            case OrderInfoService.waitReview:
                desc = "等评价";
                break;
            case OrderInfoService.finish:
                desc = "完成";
                break;
            case OrderInfoService.delete:
                desc = "刪除";
                break;
            default:
                desc = "未知";
        }
        return desc;
    }
}