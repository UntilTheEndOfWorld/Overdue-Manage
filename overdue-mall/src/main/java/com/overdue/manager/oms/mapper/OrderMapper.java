package com.overdue.manager.oms.mapper;

import java.time.LocalDateTime;
import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.h5.domain.vo.CountOrderVO;
import com.overdue.h5.domain.vo.H5OrderVO;
import com.overdue.manager.aws.domain.entity.SystemStatistics;
import com.overdue.manager.oms.domain.entity.Order;
import com.overdue.manager.oms.domain.form.ManagerOrderQueryForm;
import com.overdue.manager.oms.domain.vo.ManagerOrderVO;
import com.overdue.manager.statistics.domain.vo.OrderAndAftersaleStatisticsVO;
import com.overdue.manager.ums.domain.vo.MemberDataStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单表Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    /**
     * 查询订单表列表
     *
     * @param order 订单表
     * @return 订单表集合
     */
    List<Order> selectByEntity(Order order);

    List<ManagerOrderVO> selectManagerOrderPage(ManagerOrderQueryForm request);

    List<H5OrderVO> orderPage(@Param("status") Integer status, @Param("memberId") Long memberId);

    H5OrderVO selectOrderDetail(Long orderId);

    CountOrderVO countByStatusAndMemberId(Long memberId);

    Integer cancelBatch(@Param("list") List<Order> orderList);

    MemberDataStatisticsVO statOrderCountAndAmount(Long memberId);

    Integer statWaitDelivered();

    OrderAndAftersaleStatisticsVO statTodayData(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    SystemStatistics statNewAndDeal(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    int statDealMember(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
