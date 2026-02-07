package com.overdue.manager.pms.domain.entity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 积分商品信息对象 pms_points_product
 * 
 * @author zcc
 */
@ApiModel(description = "积分商品信息对象")
@Data
@TableName("pms_points_product")
public class PointsProduct extends BaseEntity {
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

  @ApiModelProperty("所需积分")
  @Excel(name = "所需积分")
  private BigDecimal points;

  @ApiModelProperty("原价（用于显示价值感）")
  @Excel(name = "原价")
  private BigDecimal originalPrice;

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

  @ApiModelProperty("热度值，每兑换一件商品增加1")
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

  @ApiModelProperty("商品类型：1->实物商品；2->虚拟商品；3->优惠券；4->服务")
  @Excel(name = "商品类型")
  private Integer productType;

  @ApiModelProperty("兑换限制：每人限兑数量，0表示不限制")
  @Excel(name = "兑换限制")
  private Integer exchangeLimit;

  @ApiModelProperty("总库存")
  @Excel(name = "总库存")
  private Integer totalStock;

  @ApiModelProperty("已兑换数量")
  @Excel(name = "已兑换数量")
  private Integer exchangedCount;

  @ApiModelProperty("活动开始时间")
  @Excel(name = "活动开始时间")
  private java.time.LocalDateTime activityStartTime;

  @ApiModelProperty("活动结束时间")
  @Excel(name = "活动结束时间")
  private java.time.LocalDateTime activityEndTime;

  @ApiModelProperty("是否限时活动：0->否；1->是")
  @Excel(name = "是否限时活动")
  private Integer isLimitedTime;

  @ApiModelProperty("商品描述")
  @Excel(name = "商品描述")
  private String description;

  @ApiModelProperty("删除标志（0代表存在 2代表删除）")
  @Excel(name = "删除标志")
  private String delFlag;

  /**
   * 搜索值（排除数据库字段）
   */
  @TableField(exist = false)
  private String searchValue;

  /**
   * 请求参数（排除数据库字段）
   */
  @TableField(exist = false)
  private Map<String, Object> params;

  @Override
  public String getSearchValue() {
    return searchValue;
  }

  @Override
  public void setSearchValue(String searchValue) {
    this.searchValue = searchValue;
  }

  @Override
  public Map<String, Object> getParams() {
    if (params == null) {
      params = new HashMap<>();
    }
    return params;
  }

  @Override
  public void setParams(Map<String, Object> params) {
    this.params = params;
  }
}
