package com.overdue.manager.pms.domain.entity;

import java.math.BigDecimal;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.overdue.common.core.domain.BaseAudit;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 商品信息对象 pms_product
 * 
 * @author zcc
 */
@ApiModel(description = "商品信息对象")
@Data
@TableName("pms_product")
public class Product extends BaseAudit {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("BRAND_ID")
    @Excel(name = "BRAND_ID")
    private Long brandId;

    @ApiModelProperty("CATEGORY_ID")
    @Excel(name = "CATEGORY_ID")
    private Long categoryId;

    @ApiModelProperty("商品编码")
    @Excel(name = "商品编码")
    private String outProductId;

    @ApiModelProperty("NAME")
    @Excel(name = "NAME")
    private String name;

    @ApiModelProperty("主图")
    @Excel(name = "主图")
    private String pic;

    @ApiModelProperty("画册图片，连产品图片限制为5张，以逗号分割")
    @Excel(name = "画册图片，连产品图片限制为5张，以逗号分割")
    private String albumPics;

    @ApiModelProperty("上架状态：0->下架；1->上架")
    @Excel(name = "上架状态：0->下架；1->上架")
    private Integer publishStatus;

    @ApiModelProperty("排序")
    @Excel(name = "排序")
    private Integer sort;

    @ApiModelProperty("PRICE")
    @Excel(name = "PRICE")
    private BigDecimal price;

    @ApiModelProperty("单位")
    @Excel(name = "单位")
    private String unit;

    @ApiModelProperty("商品重量，默认为克")
    @Excel(name = "商品重量，默认为克")
    private BigDecimal weight;

    @ApiModelProperty("商品销售属性，json格式")
    @Excel(name = "商品销售属性，json格式")
    private String productAttr;

    @ApiModelProperty("产品详情网页内容")
    @Excel(name = "产品详情网页内容")
    private String detailHtml;

    @ApiModelProperty("移动端网页详情")
    @Excel(name = "移动端网页详情")
    private String detailMobileHtml;

    @ApiModelProperty("品牌名称")
    @Excel(name = "品牌名称")
    private String brandName;

    @ApiModelProperty("商品分类名称")
    @Excel(name = "商品分类名称")
    private String productCategoryName;

    @ApiModelProperty("热度值，每卖出一件商品增加1")
    @Excel(name = "热度值")
    private Integer hotness;

    @ApiModelProperty("是否轮播：0->否；1->是")
    @Excel(name = "是否轮播")
    private Integer isBanner;

    @ApiModelProperty("轮播标题")
    @Excel(name = "轮播标题")
    private String bannerTitle;

    @ApiModelProperty("是否支持快递：0->不支持；1->支持")
    @Excel(name = "是否支持快递")
    private Integer supportExpress;

    @ApiModelProperty("是否原产地发货：0->否；1->是")
    @Excel(name = "是否原产地发货")
    private Integer originDelivery;

    @ApiModelProperty("价格类型：0-固定价格，1-每日调价，2-时价")
    @Excel(name = "价格类型")
    private Integer priceType;

    @ApiModelProperty("是否今日价：0->否；1->是")
    @Excel(name = "是否今日价")
    private Integer isTodayPrice;

    @ApiModelProperty("价格说明")
    @Excel(name = "价格说明")
    private String priceNote;

    @ApiModelProperty("成本价（仅后台可见）")
    @Excel(name = "成本价")
    private BigDecimal costPrice;

    @ApiModelProperty("利润率（%）")
    @Excel(name = "利润率")
    private BigDecimal profitRate;

    @ApiModelProperty("重量范围-最小值（克）")
    @Excel(name = "重量最小值")
    private BigDecimal weightRangeMin;

    @ApiModelProperty("重量范围-最大值（克）")
    @Excel(name = "重量最大值")
    private BigDecimal weightRangeMax;

}
