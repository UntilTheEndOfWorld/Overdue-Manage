package com.overdue.manager.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.oms.domain.entity.PointsOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分兑换订单Mapper接口
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Mapper
public interface PointsOrderMapper extends BaseMapper<PointsOrder> {
}
