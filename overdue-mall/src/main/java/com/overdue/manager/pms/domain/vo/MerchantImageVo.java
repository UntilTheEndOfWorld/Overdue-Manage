package com.overdue.manager.pms.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商户图片视图对象
 * 
 * @author zcc
 */
@ApiModel(description = "商户图片视图对象")
@Data
public class MerchantImageVo {

  @ApiModelProperty("图片ID")
  private Long id;

  @ApiModelProperty("商户ID")
  private Long merchantId;

  @ApiModelProperty("图片URL")
  private String imageUrl;

  @ApiModelProperty("图片类型：1-环境照片，2-产品照片，3-其他")
  private Integer imageType;

  @ApiModelProperty("图片类型名称")
  private String imageTypeName;

  @ApiModelProperty("排序")
  private Integer sort;

  @ApiModelProperty("创建时间")
  private String createTime;
}
