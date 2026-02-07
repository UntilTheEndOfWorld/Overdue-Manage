package com.overdue.manager.pms.domain.entity;

import java.math.BigDecimal;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.overdue.common.core.domain.BaseAudit;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * sku信息对象 pms_sku
 * 
 * @author zcc
 */
@ApiModel(description = "sku信息对象")
@Data
@TableName("pms_sku")
public class Sku extends BaseAudit {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("PRODUCT_ID")
    @Excel(name = "PRODUCT_ID")
    private Long productId;

    @ApiModelProperty("sku编码")
    @Excel(name = "sku编码")
    private String outSkuId;

    @ApiModelProperty("PRICE")
    @Excel(name = "PRICE")
    private BigDecimal price;

    @ApiModelProperty("展示图片")
    @Excel(name = "展示图片")
    private String pic;

    @ApiModelProperty("商品销售属性，json格式")
    @Excel(name = "商品销售属性，json格式")
    private String spData;

    @ApiModelProperty("库存数")
    @Excel(name = "库存数")
    private Integer stock;

    @ApiModelProperty("成本价（仅后台可见）")
    @Excel(name = "成本价")
    private java.math.BigDecimal costPrice;

    @ApiModelProperty("利润率（%）")
    @Excel(name = "利润率")
    private java.math.BigDecimal profitRate;

    @ApiModelProperty("SKU重量值（克）")
    @Excel(name = "重量值")
    private java.math.BigDecimal weightValue;

    @ApiModelProperty("SKU重量标签（如：小号300g、中号400g）")
    @Excel(name = "重量标签")
    private String weightLabel;

}
