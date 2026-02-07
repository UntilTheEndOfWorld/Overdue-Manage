package com.overdue.manager.item.mapper;

import com.overdue.manager.item.domain.entity.MemberOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 会员订单Mapper接口
 * 
 * @author overdue
 */
@Mapper
public interface MemberOrderMapper extends BaseMapper<MemberOrder> {
    /**
     * 查询会员订单列表
     * 
     * @param memberOrder 会员订单
     * @return 会员订单列表
     */
    List<MemberOrder> selectMemberOrderList(MemberOrder memberOrder);

    /**
     * 根据用户ID查询订单列表
     * 
     * @param userId 用户ID
     * @return 订单列表
     */
    List<MemberOrder> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据订单号查询订单
     * 
     * @param orderNo 订单号
     * @return 订单
     */
    MemberOrder selectByOrderNo(@Param("orderNo") String orderNo);
}
