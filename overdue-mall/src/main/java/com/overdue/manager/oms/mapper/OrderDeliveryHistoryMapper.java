package com.overdue.manager.oms.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.oms.domain.entity.OrderDeliveryHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单发货记录Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface OrderDeliveryHistoryMapper extends BaseMapper<OrderDeliveryHistory> {
    /**
     * 查询订单发货记录列表
     *
     * @param orderDeliveryHistory 订单发货记录
     * @return 订单发货记录集合
     */
    List<OrderDeliveryHistory> selectByEntity(OrderDeliveryHistory orderDeliveryHistory);
}
