package com.overdue.manager.pms.domain.query;

import lombok.Data;
import java.util.List;

/**
 * 积分商品查询对象
 * 
 * @author zcc
 */
@Data
public class PointsProductQuery {

  /** 分类ID */
  private Long categoryId;

  /** 上架状态 */
  private Integer publishStatus;

  /** 搜索关键词 */
  private String search;

  /** 商品类型 */
  private Integer productType;

  /** 是否轮播 */
  private Integer isBanner;

  /** 是否限时活动 */
  private Integer isLimitedTime;

  /** 排除的商品ID列表 */
  private List<Long> excludeProductIds;

  /** 包含的商品ID列表 */
  private List<Long> ids;

  /** 排序字段 */
  private String orderField;

  /** 排序方式 */
  private String orderSort;
}
