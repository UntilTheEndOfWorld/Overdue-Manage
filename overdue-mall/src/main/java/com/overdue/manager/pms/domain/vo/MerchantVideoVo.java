package com.overdue.manager.pms.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商户视频视图对象
 * 
 * @author zcc
 */
@ApiModel(description = "商户视频视图对象")
@Data
public class MerchantVideoVo {

  @ApiModelProperty("视频ID")
  private Long id;

  @ApiModelProperty("商户ID")
  private Long merchantId;

  @ApiModelProperty("视频URL")
  private String videoUrl;

  @ApiModelProperty("视频封面")
  private String videoCover;

  @ApiModelProperty("视频标题")
  private String videoTitle;

  @ApiModelProperty("视频描述")
  private String videoDescription;

  @ApiModelProperty("视频时长（秒）")
  private Integer videoDuration;

  @ApiModelProperty("视频大小（字节）")
  private Long videoSize;

  @ApiModelProperty("排序")
  private Integer sort;

  @ApiModelProperty("创建时间")
  private String createTime;
}
