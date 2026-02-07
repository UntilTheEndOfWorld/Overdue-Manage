package com.overdue.manager.pms.domain.entity;

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
 * 商户图片对象 pms_merchant_image
 * 
 * @author zcc
 */
@ApiModel(description = "商户图片对象")
@Data
@TableName("pms_merchant_image")
public class MerchantImage extends BaseEntity {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("图片ID")
  private Long id;

  @ApiModelProperty("商户ID")
  @Excel(name = "商户ID")
  private Long merchantId;

  @ApiModelProperty("图片URL")
  @Excel(name = "图片URL")
  private String imageUrl;

  @ApiModelProperty("图片类型：1-环境照片，2-产品照片，3-其他")
  @Excel(name = "图片类型", readConverterExp = "1=环境照片,2=产品照片,3=其他")
  private Integer imageType;

  @ApiModelProperty("排序")
  @Excel(name = "排序")
  private Integer sort;

  @ApiModelProperty("删除标志（0代表存在 2代表删除）")
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
