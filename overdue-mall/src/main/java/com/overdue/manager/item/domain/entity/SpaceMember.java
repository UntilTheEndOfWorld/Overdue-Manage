package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 共享空间成员表 overdue_space_member
 * 
 * @author overdue
 */
@ApiModel(description = "共享空间成员表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("overdue_space_member")
public class SpaceMember extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("成员关系ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("空间ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long spaceId;

    @ApiModelProperty("用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @ApiModelProperty("角色：creator-创建者，admin-管理员，member-成员")
    private String role;

    @ApiModelProperty("加入时间")
    private LocalDateTime joinTime;

    @ApiModelProperty("状态（0-正常 1-已退出）")
    private String status;
}
