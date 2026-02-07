package com.overdue.manager.pms.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商户分类视图对象
 * 
 * @author zcc
 */
@ApiModel(description = "商户分类视图对象")
@Data
public class MerchantCategoryVo {

  @ApiModelProperty("分类ID")
  private Long id;

  @ApiModelProperty("分类名称")
  private String categoryName;

  @ApiModelProperty("分类编码")
  private String categoryCode;

  @ApiModelProperty("父分类ID")
  private Long parentId;

  @ApiModelProperty("分类层级")
  private Integer level;

  @ApiModelProperty("排序")
  private Integer sort;

  @ApiModelProperty("状态：0-禁用，1-启用")
  private Integer status;

  @ApiModelProperty("创建时间")
  private String createTime;
}
