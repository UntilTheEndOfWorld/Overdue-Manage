package com.overdue.manager.pms.domain.vo;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商户信息视图对象
 * 
 * @author zcc
 */
@ApiModel(description = "商户信息视图对象")
@Data
public class MerchantVo {

  @ApiModelProperty("商户ID")
  private Long id;

  @ApiModelProperty("商户名称")
  private String merchantName;

  @ApiModelProperty("商户编码")
  private String merchantCode;

  @ApiModelProperty("商户简介")
  private String description;

  @ApiModelProperty("商户地址")
  private String address;

  @ApiModelProperty("联系电话")
  private String phone;

  @ApiModelProperty("邮箱")
  private String email;

  @ApiModelProperty("商户Logo")
  private String logo;

  @ApiModelProperty("封面图片")
  private String coverImage;

  @ApiModelProperty("营业执照")
  private String businessLicense;

  @ApiModelProperty("状态：0-禁用，1-启用")
  private Integer status;

  @ApiModelProperty("排序")
  private Integer sort;

  @ApiModelProperty("备注")
  private String remark;

  @ApiModelProperty("创建时间")
  private String createTime;

  @ApiModelProperty("更新时间")
  private String updateTime;

  @ApiModelProperty("商户图片列表")
  private List<MerchantImageVo> imageList;

  @ApiModelProperty("商户视频列表")
  private List<MerchantVideoVo> videoList;

  @ApiModelProperty("商户分类列表")
  private List<MerchantCategoryVo> categoryList;
}
