package com.overdue.manager.pms.domain.vo;

import com.overdue.common.core.domain.BaseAudit;
import com.overdue.manager.pms.domain.entity.PointsProductSku;
import com.overdue.common.annotation.Excel;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分商品信息 数据视图对象
 * 
 * @author zcc
 */
@Data
public class PointsProductVO extends BaseAudit {
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
  /** 所需积分 */
  @Excel(name = "所需积分")
  private BigDecimal points;
  /** 原价（用于显示价值感） */
  @Excel(name = "原价")
  private BigDecimal originalPrice;
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
  /** 热度值，每兑换一件商品增加1 */
  @Excel(name = "热度值")
  private Integer hotness;

  @Excel(name = "是否轮播")
  private Integer isBanner;

  @Excel(name = "轮播标题")
  private String bannerTitle;

  @Excel(name = "是否支持快递")
  private Integer supportExpress;

  /** 商品类型：1->实物商品；2->虚拟商品；3->优惠券；4->服务 */
  @Excel(name = "商品类型")
  private Integer productType;

  /** 兑换限制：每人限兑数量，0表示不限制 */
  @Excel(name = "兑换限制")
  private Integer exchangeLimit;

  /** 总库存 */
  @Excel(name = "总库存")
  private Integer totalStock;

  /** 已兑换数量 */
  @Excel(name = "已兑换数量")
  private Integer exchangedCount;

  /** 活动开始时间 */
  @Excel(name = "活动开始时间")
  private LocalDateTime activityStartTime;

  /** 活动结束时间 */
  @Excel(name = "活动结束时间")
  private LocalDateTime activityEndTime;

  /** 是否限时活动：0->否；1->是 */
  @Excel(name = "是否限时活动")
  private Integer isLimitedTime;

  /** 商品描述 */
  @Excel(name = "商品描述")
  private String description;

  /** 删除标志（0代表存在 2代表删除） */
  @Excel(name = "删除标志")
  private String delFlag;

  private List<PointsProductSku> skuList;
}
