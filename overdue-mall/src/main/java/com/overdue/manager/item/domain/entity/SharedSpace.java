package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 共享空间表 overdue_shared_space
 * <p>不继承 BaseEntity，避免 {@code params} Map 被当作表字段插入。</p>
 *
 * @author overdue
 */
@ApiModel(description = "共享空间表")
@Data
@TableName("overdue_shared_space")
public class SharedSpace implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty("空间ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("空间名称")
    private String name;

    @ApiModelProperty("空间描述")
    private String description;

    @ApiModelProperty("创建者用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long creatorId;

    @ApiModelProperty("邀请码")
    private String inviteCode;

    @ApiModelProperty("邀请码过期时间")
    private LocalDateTime inviteCodeExpireTime;

    @ApiModelProperty("状态（0-正常 1-已解散）")
    private String status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
