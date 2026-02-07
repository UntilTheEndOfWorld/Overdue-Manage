package com.overdue.h5.domain.vo;

import com.overdue.manager.pms.domain.vo.PointsProductSkuVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分商品VO (H5端)
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Data
@ApiModel(value = "积分商品VO")
public class H5PointsProductVO {

  @ApiModelProperty("商品ID")
  private Long id;

  @ApiModelProperty("商品名称")
  private String name;

  @ApiModelProperty("商品图片")
  private String pic;

  @ApiModelProperty("画册图片")
  private String albumPics;

  @ApiModelProperty("所需积分")
  private BigDecimal points;

  @ApiModelProperty("原价")
  private BigDecimal originalPrice;

  @ApiModelProperty("商品类型：1-实物商品，2-虚拟商品，3-优惠券，4-服务")
  private Integer productType;

  @ApiModelProperty("商品类型名称")
  private String productTypeName;

  @ApiModelProperty("总库存")
  private Integer totalStock;

  @ApiModelProperty("已兑换数量")
  private Integer exchangedCount;

  @ApiModelProperty("兑换限制：每人限兑数量，0表示不限制")
  private Integer exchangeLimit;

  @ApiModelProperty("商品描述")
  private String description;

  @ApiModelProperty("商品详情")
  private String detailHtml;

  @ApiModelProperty("移动端详情")
  private String detailMobileHtml;

  @ApiModelProperty("是否轮播：0-否，1-是")
  private Integer isBanner;

  @ApiModelProperty("轮播标题")
  private String bannerTitle;

  @ApiModelProperty("是否限时：0-否，1-是")
  private Integer isLimitedTime;

  @ApiModelProperty("活动开始时间")
  private LocalDateTime activityStartTime;

  @ApiModelProperty("活动结束时间")
  private LocalDateTime activityEndTime;

  @ApiModelProperty("上架状态：0-下架，1-上架")
  private Integer publishStatus;

  @ApiModelProperty("排序")
  private Integer sort;

  @ApiModelProperty("热度值")
  private Integer hotness;

  @ApiModelProperty("创建时间")
  private LocalDateTime createTime;

  @ApiModelProperty("SKU列表")
  private List<PointsProductSkuVO> skuList;

  @ApiModelProperty("分类名称")
  private String categoryName;

  @ApiModelProperty("品牌名称")
  private String brandName;
}
