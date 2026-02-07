package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.OperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 操作日志Mapper接口
 * 
 * @author overdue
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    /**
     * 查询操作日志列表
     * 
     * @param operationLog 操作日志
     * @return 操作日志列表
     */
    List<OperationLog> selectOperationLogList(OperationLog operationLog);

    /**
     * 根据空间ID查询操作日志
     * 
     * @param spaceId 空间ID
     * @return 操作日志列表
     */
    List<OperationLog> selectBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 根据物品ID查询操作日志
     * 
     * @param itemId 物品ID
     * @param itemType 物品类型
     * @return 操作日志列表
     */
    List<OperationLog> selectByItemId(@Param("itemId") Long itemId, 
                                     @Param("itemType") String itemType);
}
