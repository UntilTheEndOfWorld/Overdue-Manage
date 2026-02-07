package com.overdue.manager.oms.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.oms.domain.entity.OrderOperateHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单操作历史记录Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface OrderOperateHistoryMapper extends BaseMapper<OrderOperateHistory> {
    /**
     * 查询订单操作历史记录列表
     *
     * @param orderOperateHistory 订单操作历史记录
     * @return 订单操作历史记录集合
     */
    List<OrderOperateHistory> selectByEntity(OrderOperateHistory orderOperateHistory);
}
