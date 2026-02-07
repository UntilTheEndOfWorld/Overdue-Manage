package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 共享空间表 overdue_shared_space
 * 
 * @author overdue
 */
@ApiModel(description = "共享空间表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("overdue_shared_space")
public class SharedSpace extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("空间ID")
    private Long id;

    @ApiModelProperty("空间名称")
    private String name;

    @ApiModelProperty("空间描述")
    private String description;

    @ApiModelProperty("创建者用户ID")
    private Long creatorId;

    @ApiModelProperty("邀请码")
    private String inviteCode;

    @ApiModelProperty("邀请码过期时间")
    private LocalDateTime inviteCodeExpireTime;

    @ApiModelProperty("状态（0-正常 1-已解散）")
    private String status;
}
