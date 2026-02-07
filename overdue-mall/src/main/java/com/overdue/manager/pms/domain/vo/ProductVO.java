package com.overdue.manager.pms.domain.vo;

import com.overdue.manager.pms.domain.entity.Sku;
import com.overdue.common.annotation.Excel;
import com.overdue.common.core.domain.BaseAudit;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品信息 数据视图对象
 * 
 * @author zcc
 */
@Data
public class ProductVO extends BaseAudit {
    /** ID */
    private Long id;
    /** BRAND_ID */
    @Excel(name = "BRAND_ID")
    private Long brandId;
    /** CATEGORY_ID */
    @Excel(name = "CATEGORY_ID")
    private Long categoryId;
    /** 商品编码 */
    @Excel(name = "商品编码")
    private String outProductId;
    /** NAME */
    @Excel(name = "NAME")
    private String name;
    /** 主图 */
    @Excel(name = "主图")
    private String pic;
    /** 画册图片，连产品图片限制为5张，以逗号分割 */
    @Excel(name = "画册图片，连产品图片限制为5张，以逗号分割")
    private String albumPics;
    /** 上架状态：0->下架；1->上架 */
    @Excel(name = "上架状态：0->下架；1->上架")
    private Integer publishStatus;
    /** 排序 */
    @Excel(name = "排序")
    private Integer sort;
    /** PRICE */
    @Excel(name = "PRICE")
    private BigDecimal price;
    /** 单位 */
    @Excel(name = "单位")
    private String unit;
    /** 商品重量，默认为克 */
    @Excel(name = "商品重量，默认为克")
    private BigDecimal weight;
    /** 产品详情网页内容 */
    @Excel(name = "产品详情网页内容")
    private String detailHtml;
    /** 移动端网页详情 */
    @Excel(name = "移动端网页详情")
    private String detailMobileHtml;
    /** 品牌名称 */
    @Excel(name = "品牌名称")
    private String brandName;
    /** 商品分类名称 */
    @Excel(name = "商品分类名称")
    private String productCategoryName;
    @Excel(name = "商品销售属性，json格式")
    private String productAttr;
    /** 热度值，每卖出一件商品增加1 */
    @Excel(name = "热度值")
    private Integer hotness;

    @Excel(name = "是否轮播")
    private Integer isBanner;

    @Excel(name = "轮播标题")
    private String bannerTitle;

    @Excel(name = "是否支持快递")
    private Integer supportExpress;

    private List<Sku> skuList;
}
