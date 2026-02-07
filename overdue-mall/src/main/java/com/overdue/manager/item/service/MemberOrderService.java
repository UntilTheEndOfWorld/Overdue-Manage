package com.overdue.manager.item.service;

import com.overdue.manager.item.domain.entity.MemberOrder;

import java.util.List;
import java.util.Map;

/**
 * 会员订单 Service
 *
 * @author overdue
 */
public interface MemberOrderService {

    /**
     * 查询会员订单列表（后台/条件查询）
     */
    List<MemberOrder> selectMemberOrderList(MemberOrder query);

    /**
     * 根据用户ID查询订单列表
     */
    List<MemberOrder> selectByUserId(Long userId);

    /**
     * 根据ID查询订单
     */
    MemberOrder selectById(Long id);

    /**
     * 根据订单号查询
     */
    MemberOrder selectByOrderNo(String orderNo);

    /**
     * 创建会员订单（生成订单号、按套餐类型取价格）
     *
     * @param userId   用户ID（overdue_user.id）
     * @param planType 套餐类型：monthly/quarterly/yearly/lifetime
     * @return 订单实体，失败返回 null
     */
    MemberOrder createOrder(Long userId, String planType);

    /**
     * 支付成功回调：更新订单状态并开通/续期会员
     *
     * @param orderNo       订单号
     * @param transactionId 微信交易流水号
     * @return 是否处理成功
     */
    boolean onPaymentSuccess(String orderNo, String transactionId);

    /**
     * 取消订单（仅未支付可取消）
     */
    boolean cancelOrder(Long orderId, Long userId);

    /**
     * 获取套餐列表（类型 + 名称 + 价格，供前端展示）
     */
    List<Map<String, Object>> listPlanTypes();
}
