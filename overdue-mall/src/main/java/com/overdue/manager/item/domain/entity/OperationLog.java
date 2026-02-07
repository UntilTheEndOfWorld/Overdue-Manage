package com.overdue.manager.item.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 操作日志表 overdue_operation_log
 * 
 * @author overdue
 */
@ApiModel(description = "操作日志表")
@Data
@TableName("overdue_operation_log")
public class OperationLog {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("日志ID")
    private Long id;

    @ApiModelProperty("空间ID（个人物品为NULL）")
    private Long spaceId;

    @ApiModelProperty("物品ID")
    private Long itemId;

    @ApiModelProperty("物品类型：personal-个人物品，shared-共享物品")
    private String itemType;

    @ApiModelProperty("操作人用户ID")
    private Long operatorId;

    @ApiModelProperty("操作人昵称")
    private String operatorName;

    @ApiModelProperty("操作类型：add-新增，edit-编辑，delete-删除")
    private String operationType;

    @ApiModelProperty("操作描述")
    private String operationDesc;

    @ApiModelProperty("操作数据（JSON格式）")
    private String operationData;

    @ApiModelProperty("操作时间")
    private LocalDateTime operationTime;
}
