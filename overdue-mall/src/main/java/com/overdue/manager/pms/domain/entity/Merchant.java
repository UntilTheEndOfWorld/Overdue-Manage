package com.overdue.manager.pms.domain.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.overdue.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 商户信息对象 pms_merchant
 * 
 * @author zcc
 */
@ApiModel(description = "商户信息对象")
@Data
@TableName("pms_merchant")
public class Merchant extends BaseEntity {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("商户ID")
  private Long id;

  @ApiModelProperty("商户名称")
  @Excel(name = "商户名称")
  private String merchantName;

  @ApiModelProperty("商户编码")
  @Excel(name = "商户编码")
  private String merchantCode;

  @ApiModelProperty("商户简介")
  @Excel(name = "商户简介")
  private String description;

  @ApiModelProperty("商户地址")
  @Excel(name = "商户地址")
  private String address;

  @ApiModelProperty("联系电话")
  @Excel(name = "联系电话")
  private String phone;

  @ApiModelProperty("邮箱")
  @Excel(name = "邮箱")
  private String email;

  @ApiModelProperty("商户Logo")
  @Excel(name = "商户Logo")
  private String logo;

  @ApiModelProperty("封面图片")
  @Excel(name = "封面图片")
  private String coverImage;

  @ApiModelProperty("营业执照")
  @Excel(name = "营业执照")
  private String businessLicense;

  @ApiModelProperty("状态：0-禁用，1-启用")
  @Excel(name = "状态", readConverterExp = "0=禁用,1=启用")
  private Integer status;

  @ApiModelProperty("排序")
  @Excel(name = "排序")
  private Integer sort;

  @ApiModelProperty("备注")
  @Excel(name = "备注")
  private String remark;

  @ApiModelProperty("删除标志（0代表存在 2代表删除）")
  private String delFlag;

  /**
   * 搜索值（排除数据库字段）
   */
  @TableField(exist = false)
  private String searchValue;

  /**
   * 商户图片
   */
  @TableField(exist = false)
  private List<MerchantImage> imageList;

  /**
   * 商户视频
   */
  @TableField(exist = false)
  private List<MerchantVideo> videoList;

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
