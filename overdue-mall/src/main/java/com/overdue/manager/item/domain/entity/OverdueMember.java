package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 会员信息表 overdue_member
 * 
 * @author overdue
 */
@ApiModel(description = "会员信息表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("overdue_member")
public class OverdueMember extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("会员ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("套餐类型：monthly-月度，quarterly-季度，yearly-年度，lifetime-终身")
    private String planType;

    @ApiModelProperty("是否会员：0-否，1-是")
    private Integer isMember;

    @ApiModelProperty("购买时间")
    private LocalDateTime purchaseTime;

    @ApiModelProperty("到期时间（终身会员为NULL）")
    private LocalDateTime expireTime;

    @ApiModelProperty("状态（0-正常 1-已过期）")
    private String status;
}
