package com.overdue.manager.pms.domain.vo;

import java.math.BigDecimal;
import com.overdue.common.annotation.Excel;
import com.overdue.common.core.domain.BaseAudit;
import lombok.Data;
/**
 * sku信息 数据视图对象
 * 
 * @author zcc
 */
@Data
public class SkuVO extends BaseAudit {
   /** ID */
    private Long id;
   /** PRODUCT_ID */
    @Excel(name = "PRODUCT_ID")
    private Long productId;
   /** sku编码 */
    @Excel(name = "sku编码")
    private String outSkuId;
   /** PRICE */
    @Excel(name = "PRICE")
    private BigDecimal price;
   /** 展示图片 */
    @Excel(name = "展示图片")
    private String pic;
   /** 商品销售属性，json格式 */
    @Excel(name = "商品销售属性，json格式")
    private String spData;
    @Excel(name = "库存数")
    private Integer stock;
}
