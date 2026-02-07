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
 * 商户分类对象 pms_merchant_category
 * 
 * @author zcc
 */
@ApiModel(description = "商户分类对象")
@Data
@TableName("pms_merchant_category")
public class MerchantCategory extends BaseEntity {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("分类ID")
  private Long id;

  @ApiModelProperty("分类名称")
  @Excel(name = "分类名称")
  private String categoryName;

  @ApiModelProperty("分类编码")
  @Excel(name = "分类编码")
  private String categoryCode;

  @ApiModelProperty("父分类ID")
  @Excel(name = "父分类ID")
  private Long parentId;

  @ApiModelProperty("分类层级")
  @Excel(name = "分类层级")
  private Integer level;

  @ApiModelProperty("排序")
  @Excel(name = "排序")
  private Integer sort;

  @ApiModelProperty("状态：0-禁用，1-启用")
  @Excel(name = "状态", readConverterExp = "0=禁用,1=启用")
  private Integer status;

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
