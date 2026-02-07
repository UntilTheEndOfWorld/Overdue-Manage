package com.overdue.manager.item.service.impl;

import com.overdue.manager.item.domain.entity.MemberOrder;
import com.overdue.manager.item.domain.entity.OverdueMember;
import com.overdue.manager.item.mapper.MemberOrderMapper;
import com.overdue.manager.item.mapper.OverdueMemberMapper;
import com.overdue.manager.item.service.MemberOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 会员订单 Service 实现
 *
 * @author overdue
 */
@Service
public class MemberOrderServiceImpl implements MemberOrderService {

    private static final Map<String, BigDecimal> PLAN_PRICES = new LinkedHashMap<>();
    private static final Map<String, String> PLAN_NAMES = new LinkedHashMap<>();

    static {
        PLAN_PRICES.put("monthly", new BigDecimal("12.00"));
        PLAN_PRICES.put("quarterly", new BigDecimal("30.00"));
        PLAN_PRICES.put("yearly", new BigDecimal("98.00"));
        PLAN_PRICES.put("lifetime", new BigDecimal("198.00"));
        PLAN_NAMES.put("monthly", "月度会员");
        PLAN_NAMES.put("quarterly", "季度会员");
        PLAN_NAMES.put("yearly", "年度会员");
        PLAN_NAMES.put("lifetime", "终身会员");
    }

    @Autowired
    private MemberOrderMapper memberOrderMapper;
    @Autowired
    private OverdueMemberMapper overdueMemberMapper;

    @Override
    public List<MemberOrder> selectMemberOrderList(MemberOrder query) {
        if (query == null) {
            query = new MemberOrder();
        }
        return memberOrderMapper.selectMemberOrderList(query);
    }

    @Override
    public List<MemberOrder> selectByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return memberOrderMapper.selectByUserId(userId);
    }

    @Override
    public MemberOrder selectById(Long id) {
        return id == null ? null : memberOrderMapper.selectById(id);
    }

    @Override
    public MemberOrder selectByOrderNo(String orderNo) {
        return (orderNo == null || orderNo.isEmpty()) ? null : memberOrderMapper.selectByOrderNo(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberOrder createOrder(Long userId, String planType) {
        if (userId == null || planType == null || planType.isEmpty()) {
            return null;
        }
        BigDecimal price = PLAN_PRICES.get(planType.toLowerCase());
        if (price == null) {
            return null;
        }
        String orderNo = "MO" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
        MemberOrder order = new MemberOrder();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setPlanType(planType);
        order.setPrice(price);
        order.setPaymentMethod("wechat");
        order.setPaymentStatus("pending");
        order.setStatus("0");
        if (memberOrderMapper.insert(order) <= 0) {
            return null;
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean onPaymentSuccess(String orderNo, String transactionId) {
        if (orderNo == null || orderNo.isEmpty()) {
            return false;
        }
        MemberOrder order = memberOrderMapper.selectByOrderNo(orderNo);
        if (order == null || "paid".equals(order.getPaymentStatus())) {
            return true;
        }
        order.setPaymentStatus("paid");
        order.setPaymentTime(LocalDateTime.now());
        if (transactionId != null) {
            order.setTransactionId(transactionId);
        }
        if (memberOrderMapper.updateById(order) <= 0) {
            return false;
        }
        Long userId = order.getUserId();
        String planType = order.getPlanType();
        OverdueMember member = overdueMemberMapper.selectByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        if (member == null) {
            member = new OverdueMember();
            member.setUserId(userId);
            member.setPlanType(planType);
            member.setIsMember(1);
            member.setPurchaseTime(now);
            member.setExpireTime(computeExpireTime(planType, null, now));
            member.setStatus("0");
            overdueMemberMapper.insert(member);
        } else {
            member.setPlanType(planType);
            member.setIsMember(1);
            member.setPurchaseTime(now);
            member.setExpireTime(computeExpireTime(planType, member.getExpireTime(), now));
            member.setStatus("0");
            overdueMemberMapper.updateById(member);
        }
        return true;
    }

    private static LocalDateTime computeExpireTime(String planType, LocalDateTime currentExpire, LocalDateTime now) {
        if ("lifetime".equalsIgnoreCase(planType)) {
            return null;
        }
        LocalDateTime base = (currentExpire != null && currentExpire.isAfter(now)) ? currentExpire : now;
        switch (planType.toLowerCase()) {
            case "monthly":
                return base.plusMonths(1);
            case "quarterly":
                return base.plusMonths(3);
            case "yearly":
                return base.plusYears(1);
            default:
                return base.plusMonths(1);
        }
    }

    @Override
    public boolean cancelOrder(Long orderId, Long userId) {
        if (orderId == null) {
            return false;
        }
        MemberOrder order = memberOrderMapper.selectById(orderId);
        if (order == null || !"pending".equals(order.getPaymentStatus())) {
            return false;
        }
        if (userId != null && !userId.equals(order.getUserId())) {
            return false;
        }
        order.setStatus("1");
        return memberOrderMapper.updateById(order) > 0;
    }

    @Override
    public List<Map<String, Object>> listPlanTypes() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String type : PLAN_NAMES.keySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("planType", type);
            m.put("name", PLAN_NAMES.get(type));
            m.put("price", PLAN_PRICES.get(type));
            list.add(m);
        }
        return list;
    }
}
