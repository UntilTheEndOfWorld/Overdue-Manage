package com.overdue.manager.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 个人物品状态统计（与小程序 item.js 中过期/即将过期判定一致）
 */
@Data
@ApiModel("个人物品统计")
public class PersonalItemStatsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("全部（未删除）")
    private long total;

    @ApiModelProperty("正常（距过期超过 7 天或无过期日期的余量）")
    private long normal;

    @ApiModelProperty("即将过期（7 天内且未过期）")
    private long near;

    @ApiModelProperty("已过期")
    private long expired;
}
