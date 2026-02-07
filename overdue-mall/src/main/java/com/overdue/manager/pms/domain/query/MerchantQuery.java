package com.overdue.manager.pms.domain.query;

import com.overdue.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商户信息查询对象
 * 
 * @author zcc
 */
@ApiModel(description = "商户信息查询对象")
@Data
public class MerchantQuery extends BaseEntity {

  @ApiModelProperty("商户名称")
  private String merchantName;

  @ApiModelProperty("商户编码")
  private String merchantCode;

  @ApiModelProperty("状态：0-禁用，1-启用")
  private Integer status;

  @ApiModelProperty("地址关键词")
  private String addressKeyword;

  @ApiModelProperty("电话")
  private String phone;
}
