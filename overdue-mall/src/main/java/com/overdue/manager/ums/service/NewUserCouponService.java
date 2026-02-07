package com.overdue.manager.ums.service;

import com.overdue.manager.act.domain.entity.CouponActivity;
import com.overdue.manager.act.domain.entity.MemberCoupon;
import com.overdue.manager.act.mapper.CouponActivityMapper;
import com.overdue.manager.act.mapper.MemberCouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 新用户优惠券赠送服务
 * 
 * @author system
 */
@Slf4j
@Service
public class NewUserCouponService {

  @Autowired
  private CouponActivityMapper couponActivityMapper;

  @Autowired
  private MemberCouponMapper memberCouponMapper;

  /**
   * 为新用户赠送优惠券
   * 
   * @param memberId 用户ID
   * @return 是否赠送成功
   */
  @Transactional
  public boolean giveNewUserCoupons(Long memberId) {
    try {
      log.info("开始为新用户 {} 赠送优惠券", memberId);

      // 创建三张新人优惠券
      List<CouponActivity> newUserCoupons = Arrays.asList(
          createNewUserCoupon("新人专享券", new BigDecimal("1"), new BigDecimal("10")),
          createNewUserCoupon("新人专享券", new BigDecimal("2"), new BigDecimal("20")),
          createNewUserCoupon("新人专享券", new BigDecimal("5"), new BigDecimal("50")));

      // 批量插入优惠券活动
      for (CouponActivity coupon : newUserCoupons) {
        int result = couponActivityMapper.insert(coupon);
        if (result <= 0) {
          log.error("插入优惠券活动失败: {}", coupon.getTitle());
          throw new RuntimeException("优惠券活动创建失败");
        }
      }

      // 批量插入用户优惠券
      for (CouponActivity coupon : newUserCoupons) {
        MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.setMemberId(memberId);
        memberCoupon.setCouponActivityId(coupon.getId());
        memberCoupon.setTitle(coupon.getTitle());
        memberCoupon.setCouponAmount(coupon.getCouponAmount());
        memberCoupon.setMinAmount(coupon.getMinAmount());
        memberCoupon.setUseScope(1); // 全场通用
        memberCoupon.setUseStatus(0); // 未使用
        memberCoupon.setCouponType(1); // 免费兑换
        memberCoupon.setUseIntegral(BigDecimal.ZERO); // 不需要积分
        memberCoupon.setBeginTime(LocalDateTime.now());
        memberCoupon.setEndTime(LocalDateTime.now().plusDays(30)); // 30天有效期
        memberCoupon.setCreateTime(LocalDateTime.now());
        //memberCoupon.setUpdateTime(LocalDateTime.now());

        int result = memberCouponMapper.insert(memberCoupon);
        if (result <= 0) {
          log.error("插入用户优惠券失败: {}", memberCoupon.getTitle());
          throw new RuntimeException("用户优惠券创建失败");
        }
      }

      log.info("新用户 {} 优惠券赠送成功，共赠送 {} 张", memberId, newUserCoupons.size());
      return true;

    } catch (Exception e) {
      log.error("新用户优惠券赠送失败: {}", e.getMessage(), e);
      throw new RuntimeException("优惠券赠送失败: " + e.getMessage());
    }
  }

  /**
   * 创建新人优惠券活动
   * 
   * @param title        优惠券标题
   * @param couponAmount 优惠金额
   * @param minAmount    最低消费金额
   * @return 优惠券活动对象
   */
  private CouponActivity createNewUserCoupon(String title, BigDecimal couponAmount, BigDecimal minAmount) {
    CouponActivity coupon = new CouponActivity();
    coupon.setTitle(title);
    coupon.setUseScope(1); // 全场通用
    coupon.setProductIds(""); // 无指定商品
    coupon.setTotalCount(10000); // 发行总数
    coupon.setLeftCount(10000); // 剩余数量
    coupon.setUserLimit(1); // 每人限领1张
    coupon.setCouponAmount(couponAmount);
    coupon.setMinAmount(minAmount);
    coupon.setUseIntegral(BigDecimal.ZERO); // 免费兑换
    coupon.setCouponType(1); // 免费兑换
    coupon.setBeginTime(LocalDateTime.now());
    coupon.setEndTime(LocalDateTime.now().plusDays(365)); // 活动有效期1年
    coupon.setStatus(1); // 启用状态
    coupon.setCreateTime(LocalDateTime.now());
    coupon.setUpdateTime(LocalDateTime.now());
    //coupon.setCreateBy(1L); // 系统创建
    //coupon.setUpdateBy(1L);

    return coupon;
  }
}
