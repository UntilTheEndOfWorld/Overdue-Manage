package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.OperationLog;
import java.util.List;

/**
 * 操作日志Service接口
 * 
 * @author overdue
 */
public interface OperationLogService {
    /**
     * 查询操作日志列表
     * 
     * @param operationLog 操作日志
     * @return 操作日志列表
     */
    List<OperationLog> selectOperationLogList(OperationLog operationLog);

    /**
     * 新增操作日志
     * 
     * @param operationLog 操作日志
     * @return 结果
     */
    int insertOperationLog(OperationLog operationLog);

    /**
     * 根据空间ID查询操作日志
     * 
     * @param spaceId 空间ID
     * @return 操作日志列表
     */
    List<OperationLog> selectBySpaceId(Long spaceId);

    /**
     * 根据物品ID查询操作日志
     * 
     * @param itemId 物品ID
     * @param itemType 物品类型
     * @return 操作日志列表
     */
    List<OperationLog> selectByItemId(Long itemId, String itemType);
}
