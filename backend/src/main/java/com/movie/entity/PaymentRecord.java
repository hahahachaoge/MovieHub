package com.movie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("order_no")
    private String orderNo;

    private BigDecimal amount;

    @TableField("pay_type")
    private String payType;

    private String status;

    private String subject;

    private String body;

    @TableField("trade_no")
    private String tradeNo;

    @TableField("notify_time")
    private LocalDateTime notifyTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
