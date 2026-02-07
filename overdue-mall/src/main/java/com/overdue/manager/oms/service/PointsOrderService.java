package com.overdue.manager.oms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.manager.oms.domain.entity.PointsOrder;
import com.overdue.manager.oms.domain.form.DeliverPointsOrderForm;
import com.overdue.manager.oms.domain.form.PointsOrderQuery;
import com.overdue.manager.oms.domain.vo.PointsOrderVO;
import com.overdue.manager.oms.mapper.PointsOrderMapper;
import com.overdue.manager.pms.domain.entity.PointsProduct;
import com.overdue.manager.pms.domain.vo.PointsProductVO;
import com.overdue.manager.pms.service.PointsProductService;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.domain.entity.MemberAddress;
import com.overdue.manager.ums.mapper.MemberAddressMapper;
import com.overdue.manager.ums.mapper.MemberMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分兑换订单服务
 * 
 * @author zcc
 * @date 2024-01-15
 */
@Service
@Slf4j
public class PointsOrderService {

  @Autowired
  private PointsOrderMapper pointsOrderMapper;

  @Autowired
  private PointsProductService pointsProductService;

  @Autowired
  private MemberMapper memberMapper;

  @Autowired
  private MemberAddressMapper memberAddressMapper;

  /**
   * 获取PointsOrderMapper（供其他服务使用）
   */
  public PointsOrderMapper getPointsOrderMapper() {
    return pointsOrderMapper;
  }

  /**
   * 创建积分兑换订单
   */
  @Transactional
  public AjaxResult createPointsOrder(Long memberId, Long pointsProductId, Integer quantity, Long addressId,
      String note) {
    try {
      // 1. 获取用户信息
      Member member = memberMapper.selectById(memberId);
      if (member == null) {
        return AjaxResult.error("用户不存在");
      }

      // 2. 获取积分商品信息
      PointsProductVO product = pointsProductService.selectById(pointsProductId);
      if (product == null) {
        return AjaxResult.error("商品不存在");
      }

      if (product.getPublishStatus() != 1) {
        return AjaxResult.error("商品已下架");
      }

      // 3. 检查库存
      if (product.getTotalStock() < quantity) {
        return AjaxResult.error("库存不足");
      }

      // 4. 检查用户积分
      BigDecimal requiredPoints = product.getPoints().multiply(new BigDecimal(quantity));
      if (member.getIntegral().compareTo(requiredPoints) < 0) {
        return AjaxResult.error("积分不足，需要" + requiredPoints + "积分");
      }

      // 5. 获取收货地址
      MemberAddress address = null;
      if (addressId != null) {
        address = memberAddressMapper.selectById(addressId);
        if (address == null) {
          return AjaxResult.error("收货地址不存在");
        }
      }

      // 6. 生成订单号
      String orderSn = "PO" + System.currentTimeMillis();

      // 7. 创建积分兑换订单
      PointsOrder pointsOrder = new PointsOrder();
      pointsOrder.setOrderSn(orderSn);
      pointsOrder.setMemberId(memberId);
      pointsOrder.setMemberUsername(member.getNickname());
      pointsOrder.setPointsProductId(pointsProductId);
      pointsOrder.setPointsProductName(product.getName());
      pointsOrder.setQuantity(quantity);
      pointsOrder.setPointsUsed(requiredPoints);
      // 0-待发货
      pointsOrder.setStatus(0);
      pointsOrder.setNote(note);
      pointsOrder.setConfirmStatus(0);
      pointsOrder.setDeleteStatus(0);
      pointsOrder.setCreateTime(LocalDateTime.now());
      pointsOrder.setCreateBy(memberId);

      // 设置收货地址信息
      if (address != null) {
        pointsOrder.setReceiverName(address.getName());
        pointsOrder.setReceiverPhone(address.getPhoneHidden());
        pointsOrder.setReceiverPostCode(address.getPostCode());
        pointsOrder.setReceiverProvince(address.getProvince());
        pointsOrder.setReceiverCity(address.getCity());
        pointsOrder.setReceiverDistrict(address.getDistrict());
        pointsOrder.setReceiverDetailAddress(address.getDetailAddress());
      }

      int rows = pointsOrderMapper.insert(pointsOrder);
      if (rows < 1) {
        return AjaxResult.error("创建订单失败");
      }

      // 8. 扣除用户积分
      member.setIntegral(member.getIntegral().subtract(requiredPoints));
      memberMapper.updateById(member);

      // 9. 更新商品库存和兑换数量
      pointsProductService.increaseExchangedCount(pointsProductId, quantity);

      log.info("用户{}成功创建积分兑换订单{}，消耗积分{}", memberId, orderSn, requiredPoints);

      return AjaxResult.success("订单创建成功", pointsOrder.getId());

    } catch (Exception e) {
      log.error("创建积分兑换订单失败", e);
      return AjaxResult.error("创建订单失败：" + e.getMessage());
    }
  }

  /**
   * 查询积分兑换订单列表
   */
  public PageImpl<PointsOrderVO> selectList(PointsOrderQuery query, Pageable pageable) {
    QueryWrapper<PointsOrder> wrapper = new QueryWrapper<>();

    if (query.getOrderSn() != null) {
      wrapper.like("order_sn", query.getOrderSn());
    }
    if (query.getMemberId() != null) {
      wrapper.eq("member_id", query.getMemberId());
    }
    if (query.getMemberUsername() != null) {
      wrapper.like("member_username", query.getMemberUsername());
    }
    if (query.getPointsProductId() != null) {
      wrapper.eq("points_product_id", query.getPointsProductId());
    }
    if (query.getPointsProductName() != null) {
      wrapper.like("points_product_name", query.getPointsProductName());
    }
    if (query.getStatus() != null) {
      wrapper.eq("status", query.getStatus());
    }
    if (query.getReceiverName() != null) {
      wrapper.like("receiver_name", query.getReceiverName());
    }
    if (query.getReceiverPhone() != null) {
      wrapper.like("receiver_phone", query.getReceiverPhone());
    }
    if (query.getCreateTimeStart() != null) {
      wrapper.ge("create_time", query.getCreateTimeStart());
    }
    if (query.getCreateTimeEnd() != null) {
      wrapper.le("create_time", query.getCreateTimeEnd());
    }

    wrapper.orderByDesc("create_time");

    Page<PointsOrder> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
    Page<PointsOrder> orderPage = pointsOrderMapper.selectPage(page, wrapper);

    List<PointsOrderVO> orderVOs = orderPage.getRecords().stream()
        .map(this::convertToPointsOrderVO)
        .collect(Collectors.toList());

    return new PageImpl<>(orderVOs, pageable, orderPage.getTotal());
  }

  /**
   * 根据ID查询积分兑换订单
   */
  public PointsOrderVO selectById(Long id) {
    PointsOrder order = pointsOrderMapper.selectById(id);
    if (order == null) {
      return null;
    }
    return convertToPointsOrderVO(order);
  }

  /**
   * 发货
   */
  @Transactional
  public AjaxResult deliverOrder(DeliverPointsOrderForm form, Long userId) {
    try {
      PointsOrder order = pointsOrderMapper.selectById(form.getOrderId());
      if (order == null) {
        return AjaxResult.error("订单不存在");
      }

      if (order.getStatus() != 0) {
        return AjaxResult.error("订单状态错误，无法发货");
      }

      // 更新订单状态
      // 1-已发货
      order.setStatus(1);
      order.setDeliveryCompany(form.getExpressName());
      order.setDeliverySn(form.getExpressSn());
      order.setDeliveryTime(LocalDateTime.now());
      order.setMerchantNote(form.getMerchantNote());
      order.setUpdateTime(LocalDateTime.now());
      order.setUpdateBy(userId);

      int rows = pointsOrderMapper.updateById(order);
      if (rows < 1) {
        return AjaxResult.error("发货失败");
      }

      log.info("积分兑换订单{}发货成功，物流公司：{}，物流单号：{}", order.getOrderSn(), form.getExpressName(), form.getExpressSn());

      return AjaxResult.success("发货成功");

    } catch (Exception e) {
      log.error("积分兑换订单发货失败", e);
      return AjaxResult.error("发货失败：" + e.getMessage());
    }
  }

  /**
   * 确认收货
   */
  @Transactional
  public AjaxResult confirmReceive(Long orderId, Long memberId) {
    try {
      PointsOrder order = pointsOrderMapper.selectById(orderId);
      if (order == null) {
        return AjaxResult.error("订单不存在");
      }

      if (!order.getMemberId().equals(memberId)) {
        return AjaxResult.error("无权限操作此订单");
      }

      if (order.getStatus() != 1) {
        return AjaxResult.error("订单状态错误，无法确认收货");
      }

      // 更新订单状态
      // 2-已收货
      order.setStatus(2);
      order.setReceiveTime(LocalDateTime.now());
      order.setConfirmStatus(1);
      order.setUpdateTime(LocalDateTime.now());
      order.setUpdateBy(memberId);

      int rows = pointsOrderMapper.updateById(order);
      if (rows < 1) {
        return AjaxResult.error("确认收货失败");
      }

      log.info("积分兑换订单{}确认收货成功", order.getOrderSn());

      return AjaxResult.success("确认收货成功");

    } catch (Exception e) {
      log.error("积分兑换订单确认收货失败", e);
      return AjaxResult.error("确认收货失败：" + e.getMessage());
    }
  }

  /**
   * 完成订单
   */
  @Transactional
  public AjaxResult completeOrder(Long orderId, Long userId) {
    try {
      PointsOrder order = pointsOrderMapper.selectById(orderId);
      if (order == null) {
        return AjaxResult.error("订单不存在");
      }

      if (order.getStatus() != 2) {
        return AjaxResult.error("订单状态错误，无法完成订单");
      }

      // 更新订单状态
      // 3-已完成
      order.setStatus(3);
      order.setUpdateTime(LocalDateTime.now());
      order.setUpdateBy(userId);

      int rows = pointsOrderMapper.updateById(order);
      if (rows < 1) {
        return AjaxResult.error("完成订单失败");
      }

      log.info("积分兑换订单{}完成", order.getOrderSn());

      return AjaxResult.success("订单完成");

    } catch (Exception e) {
      log.error("积分兑换订单完成失败", e);
      return AjaxResult.error("完成订单失败：" + e.getMessage());
    }
  }

  /**
   * 转换为VO
   */
  private PointsOrderVO convertToPointsOrderVO(PointsOrder order) {
    PointsOrderVO vo = new PointsOrderVO();
    BeanUtils.copyProperties(order, vo);

    // 设置状态名称
    vo.setStatusName(getStatusName(order.getStatus()));

    // 设置完整收货地址
    if (order.getReceiverProvince() != null && order.getReceiverCity() != null &&
        order.getReceiverDistrict() != null && order.getReceiverDetailAddress() != null) {
      vo.setFullAddress(order.getReceiverProvince() + order.getReceiverCity() +
          order.getReceiverDistrict() + order.getReceiverDetailAddress());
    }

    return vo;
  }

  /**
   * 获取状态名称
   */
  private String getStatusName(Integer status) {
    if (status == null) {
      return "";
    }
    switch (status) {
      case 0:
        return "待发货";
      case 1:
        return "已发货";
      case 2:
        return "已收货";
      case 3:
        return "已完成";
      case 4:
        return "已取消";
      default:
        return "未知状态";
    }
  }
}
