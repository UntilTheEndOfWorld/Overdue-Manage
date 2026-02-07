package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.overdue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;

/**
 * 个人物品表 overdue_personal_item
 * 
 * @author overdue
 */
@ApiModel(description = "个人物品表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("overdue_personal_item")
public class PersonalItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("物品ID")
    private Long id;

    @ApiModelProperty("用户ID")
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

    @ApiModelProperty("保质期单位：天、月、年")
    private String shelfLifeUnit;

    @ApiModelProperty("过期日期")
    private LocalDate expiryDate;

    @ApiModelProperty("状态（0-正常 1-已删除）")
    private String status;
}
