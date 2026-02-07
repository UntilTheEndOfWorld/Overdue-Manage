package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 广告观看记录表 overdue_ad_watch_record
 * 
 * @author overdue
 */
@ApiModel(description = "广告观看记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("overdue_ad_watch_record")
public class AdWatchRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("记录ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("观看次数（获得的额度）")
    private Integer watchCount;

    @ApiModelProperty("已使用次数")
    private Integer usedCount;

    @ApiModelProperty("最后观看时间")
    private LocalDateTime lastWatchTime;
}
