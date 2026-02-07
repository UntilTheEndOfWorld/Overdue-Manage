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
 * 商户视频对象 pms_merchant_video
 * 
 * @author zcc
 */
@ApiModel(description = "商户视频对象")
@Data
@TableName("pms_merchant_video")
public class MerchantVideo extends BaseEntity {
  private static final long serialVersionUID = 1L;

  @ApiModelProperty("视频ID")
  private Long id;

  @ApiModelProperty("商户ID")
  @Excel(name = "商户ID")
  private Long merchantId;

  @ApiModelProperty("视频URL")
  @Excel(name = "视频URL")
  private String videoUrl;

  @ApiModelProperty("视频封面")
  @Excel(name = "视频封面")
  private String videoCover;

  @ApiModelProperty("视频标题")
  @Excel(name = "视频标题")
  private String videoTitle;

  @ApiModelProperty("视频描述")
  @Excel(name = "视频描述")
  private String videoDescription;

  @ApiModelProperty("视频时长（秒）")
  @Excel(name = "视频时长（秒）")
  private Integer videoDuration;

  @ApiModelProperty("视频大小（字节）")
  @Excel(name = "视频大小（字节）")
  private Long videoSize;

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
