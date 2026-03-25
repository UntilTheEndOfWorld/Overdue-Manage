package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人物品表 overdue_personal_item
 * <p>不继承 BaseEntity，避免 {@code params} Map 被当作表字段插入。</p>
 *
 * @author overdue
 */
@ApiModel(description = "个人物品表")
@Data
@TableName("overdue_personal_item")
public class PersonalItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty("物品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @ApiModelProperty("物品名称")
    private String name;

    @ApiModelProperty("分类：食品、药品、日用品")
    private String category;

    @ApiModelProperty("购买日期")
    private LocalDate purchaseDate;

    @ApiModelProperty("生产日期")
    private LocalDate productionDate;

    @ApiModelProperty("保质期数值")
    private Integer shelfLife;

    /** 小程序 JSON 字段名为 unit；管理端可为 shelfLifeUnit */
    @ApiModelProperty("保质期单位：天、月、年")
    @JsonProperty("unit")
    @JsonAlias({ "shelfLifeUnit" })
    private String shelfLifeUnit;

    @ApiModelProperty("过期日期")
    private LocalDate expiryDate;

    @ApiModelProperty("状态（0-正常 1-已删除）")
    private String status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
