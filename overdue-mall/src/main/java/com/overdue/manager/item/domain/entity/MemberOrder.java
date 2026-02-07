package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员订单表 overdue_member_order
 * 
 * @author overdue
 */
@ApiModel(description = "会员订单表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("overdue_member_order")
public class MemberOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("套餐类型：monthly-月度，quarterly-季度，yearly-年度，lifetime-终身")
    private String planType;

    @ApiModelProperty("订单金额")
    private BigDecimal price;

    @ApiModelProperty("支付方式：wechat-微信支付")
    private String paymentMethod;

    @ApiModelProperty("支付状态：pending-未支付，paid-已支付，failed-支付失败，refunded-已退款")
    private String paymentStatus;

    @ApiModelProperty("支付时间")
    private LocalDateTime paymentTime;

    @ApiModelProperty("交易流水号")
    private String transactionId;

    @ApiModelProperty("订单状态：0-正常，1-已取消")
    private String status;
}
