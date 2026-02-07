package com.overdue.h5.domain.form;

import com.overdue.h5.domain.dto.OrderProductListDTO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitForm {
    private Long addressId;
    private String note;
    /** 支付方式 0：未支付 1：支付宝 2：微信 默认微信 */
    private Integer payType = 2;
    /** 订单来源，购物车则为cart */
    private String from;
    private Long memberCouponId;
    /** 运费金额 */
    private BigDecimal freightAmount = BigDecimal.ZERO;
    /** 配送方式：express-快递配送，pickup-到店取货，local-同城配送 */
    private String deliveryMethod = "express";
    @NotEmpty
    private List<OrderProductListDTO> skuList;

    @Data
    public static class SkuParam {
        private Long skuId;
        private Integer quantity;
    }
}
