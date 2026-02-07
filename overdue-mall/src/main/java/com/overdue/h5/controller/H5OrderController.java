package com.overdue.h5.controller;

import com.alibaba.fastjson.JSONObject;
import com.overdue.h5.domain.dto.DeliveryReq;
import com.overdue.h5.domain.form.*;
import com.overdue.h5.domain.vo.*;
import com.overdue.h5.service.H5OrderService;
import com.overdue.manager.oms.domain.entity.Aftersale;
import com.overdue.manager.oms.domain.entity.Order;
import com.overdue.manager.oms.domain.form.DealWithAftersaleForm;
import com.overdue.manager.oms.service.AftersaleService;
import com.overdue.manager.oms.service.OrderService;
import com.overdue.manager.oms.service.OrderItemService;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.redis.OrderCountdownService;
import com.overdue.common.core.redis.RedisService;
import com.overdue.common.enums.AftersaleStatus;
import com.overdue.common.enums.OrderStatus;
import com.overdue.framework.config.LocalDataUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/h5/order")
@Slf4j
public class H5OrderController {
    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderCountdownService orderCountdownService;
    @Autowired
    private H5OrderService service;
    @Autowired
    private AftersaleService aftersaleService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    // 开发模式标志 - 已注释，确保使用正常业务逻辑
    // private static final boolean DEV_MODE = false;

    @ApiOperation("下单")
    @PostMapping("/add")
    public ResponseEntity<Long> submit(@RequestBody OrderSubmitForm form) {
        log.info("创建订单 - 商品数量: " + (form.getSkuList() != null ? form.getSkuList().size() : 0));

        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        Long memberId = member.getId();
        String redisKey = "h5_order_add" + memberId;
        String redisValue = memberId + "_" + System.currentTimeMillis();
        try {
            redisService.lock(redisKey, redisValue, 60);
            return ResponseEntity.ok(service.submit(form));
        } catch (Exception e) {
            log.info("创建订单方法异常", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @ApiOperation("下单前校验")
    @PostMapping("/addOrderCheck")
    public ResponseEntity<OrderCalcVO> addOrderCheck(@RequestBody OrderCreateForm orderCreateForm) {
        return ResponseEntity.ok(service.addOrderCheck(orderCreateForm));
    }

    @ApiOperation("订单列表")
    @GetMapping("/page")
    public ResponseEntity<PageImpl<H5OrderVO>> orderPage(Integer status, Pageable pageable) {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        return ResponseEntity.ok(service.orderPage(status, member.getId(), pageable));
    }

    @ApiOperation("订单详情")
    @GetMapping("/orderDetail")
    public ResponseEntity<H5OrderVO> orderDetail(@RequestParam(required = false) Long orderId) {
        if (orderId == null) {
            throw new RuntimeException("系统繁忙");
        }

        return ResponseEntity.ok(service.orderDetail(orderId));
    }

    @ApiOperation("通过支付ID查询订单详情")
    @GetMapping("/orderDetailByPayId")
    public ResponseEntity<H5OrderVO> orderDetailByPayId(@RequestParam String payId) {
        if (payId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.orderDetailByPayId(payId));
    }

    @ApiOperation("通过商户订单号查询订单详情")
    @GetMapping("/orderDetailByOutTradeNo")
    public ResponseEntity<H5OrderVO> orderDetailByOutTradeNo(@RequestParam String outTradeNo) {
        if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
            throw new RuntimeException("商户订单号不能为空");
        }
        return ResponseEntity.ok(service.orderDetailByOutTradeNo(outTradeNo));
    }

    @ApiOperation("确认收货")
    @GetMapping("/orderComplete")
    public ResponseEntity<String> orderComplete(Long orderId) {
        log.info("确认收货，订单id：" + orderId);

        String redisKey = "h5_oms_order_complete_" + orderId;
        String redisValue = orderId + "_" + System.currentTimeMillis();
        try {
            redisService.lock(redisKey, redisValue, 60);
            return ResponseEntity.ok(service.orderComplete(orderId));
        } catch (Exception e) {
            log.error("确认收货异常", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    @ApiOperation("订单数量统计")
    @GetMapping("/countOrder")
    public AjaxResult orderNumCount() {
        try {
            Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
            if (member == null) {
                return AjaxResult.error("用户信息不存在");
            }

            Long memberId = member.getId();

            // 获取各状态订单数量
            Map<String, Object> orderCount = new HashMap<>();

            // 普通订单数量统计
            int waitPayCount = service.countOrderByMemberAndStatus(memberId, 0);
            int waitDeliveryCount = service.countOrderByMemberAndStatus(memberId, 1);
            int waitReceiveCount = service.countOrderByMemberAndStatus(memberId, 2);
            int completeCount = service.countOrderByMemberAndStatus(memberId, 3);
            int cancelledCount = service.countOrderByMemberAndStatus(memberId, 4);

            // 积分商城订单数量统计
            // 积分商城订单状态：0-待发货，1-已发货，2-已收货，3-已完成
            // 普通订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-已取消
            // 积分商城待发货
            int pointsWaitDeliveryCount = service.countPointsOrderByMemberAndStatus(memberId, 0);
            // 积分商城已发货
            int pointsWaitReceiveCount = service.countPointsOrderByMemberAndStatus(memberId, 1);
            // 积分商城已收货+已完成
            int pointsCompleteCount = service.countPointsOrderByMemberAndStatus(memberId, 2) +
                    service.countPointsOrderByMemberAndStatus(memberId, 3);

            // 合并订单数量（待支付订单不需要相加）
            // 只有普通订单有待支付
            orderCount.put("waitPayCount", waitPayCount);
            orderCount.put("waitDeliveryCount", waitDeliveryCount + pointsWaitDeliveryCount);
            orderCount.put("waitReceiveCount", waitReceiveCount + pointsWaitReceiveCount);
            orderCount.put("completeCount", completeCount + pointsCompleteCount);
            // 只有普通订单有已取消
            orderCount.put("cancelledCount", cancelledCount);

            // 总订单数量（待支付订单不计算在内）
            int totalCount = (Integer) orderCount.get("waitDeliveryCount")
                    + (Integer) orderCount.get("waitReceiveCount")
                    + (Integer) orderCount.get("completeCount");
            orderCount.put("totalCount", totalCount);

            log.info("获取用户{}订单数量统计成功: {}", memberId, orderCount);

            return AjaxResult.successData(orderCount);
        } catch (Exception e) {
            log.error("获取订单数量统计失败", e);
            return AjaxResult.error("获取订单数量统计失败");
        }
    }

    @ApiOperation("取消订单")
    @PostMapping("/orderCancel")
    public AjaxResult orderCancel(@RequestBody CancelOrderForm request) {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        String redisKey = "h5_oms_order_cancel_" + request.getIdList().get(0);
        String redisValue = request.getIdList().get(0) + "_" + System.currentTimeMillis();
        try {
            // 尝试获取锁，如果获取失败会抛出异常
            redisService.lock(redisKey, redisValue, 60);

            String result = service.orderBatchCancel(request, member.getId());
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("订单取消方法异常", e);
            // 如果是重复提交异常，返回更友好的错误信息
            if (e.getMessage() != null && e.getMessage().contains("反复提交")) {
                return AjaxResult.error("订单正在处理中，请稍后再试");
            }
            return AjaxResult.error(e.getMessage());
        } finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                log.error("释放订单取消锁失败", e);
            }
        }
    }

    @ApiOperation("订单支付")
    @PostMapping("/orderPay")
    public ResponseEntity<OrderPayVO> orderPay(@RequestBody OrderPayForm req) {
        log.info("订单支付", "提交的数据：" + JSONObject.toJSONString(req));

        String redisKey = "h5_oms_order_pay_" + req.getOrderId();
        String redisValue = req.getOrderId() + "_" + System.currentTimeMillis();
        try {
            redisService.lock(redisKey, redisValue, 60);
            Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
            Long memberId = member.getId();
            req.setMemberId(memberId);
            return ResponseEntity.ok(service.orderPay(req));
        } catch (Exception e) {
            log.error("支付方法异常", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    @ApiOperation("申请售后")
    @PostMapping("/applyRefund")
    public ResponseEntity<Boolean> applyRefund(@RequestBody ApplyRefundForm applyRefundForm) {
        String redisKey = "h5_oms_order_applyRefund_" + applyRefundForm.getOrderId();
        String redisValue = applyRefundForm.getOrderId() + "_" + System.currentTimeMillis();
        try {
            redisService.lock(redisKey, redisValue, 60);
            Order order = service.applyRefund(applyRefundForm);
            // 如果是未发货，系统自动退款
            if (order.getStatus().equals(OrderStatus.NOT_DELIVERED.getType())) {
                DealWithAftersaleForm req = new DealWithAftersaleForm();
                req.setOrderId(applyRefundForm.getOrderId());
                req.setOptType(1);
                aftersaleService.dealWith(req, order.getMemberId(), "直接发起退款");
            }
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            log.error("申请售后发生异常", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    @ApiOperation("取消售后")
    @GetMapping("/cancelRefund")
    public ResponseEntity<String> cancelRefund(Long orderId) {
        log.info("【取消售后】订单id：" + orderId);
        String redisKey = "h5_oms_order_cancelRefund_" + orderId;
        String redisValue = orderId + "_" + System.currentTimeMillis();
        try {
            redisService.lock(redisKey, redisValue, 60);
            return ResponseEntity.ok(service.cancelRefund(orderId));
        } catch (Exception e) {
            log.error("取消售后发生异常", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    @ApiOperation("售后订单详情")
    @GetMapping("/refundOrderDetail")
    public ResponseEntity<AftersaleRefundInfoVO> refundOrderDetail(@RequestParam Long orderId) {
        return ResponseEntity.ok(service.refundOrderDetail(orderId));
    }

    @ApiOperation("用户提交退货单号")
    @PostMapping("/aftersale/delivery")
    public AjaxResult delivery(@RequestBody @Valid DeliveryReq req) {
        log.info("用户提交退货单号", "提交的数据：" + JSONObject.toJSONString(req));
        String redisKey = "h5_oms_order_delivery_" + req.getOrderId();
        String redisValue = req.getOrderId() + "_" + System.currentTimeMillis();
        try {
            redisService.lock(redisKey, redisValue, 60);
            Order order = service.selectById(req.getOrderId());
            Aftersale aftersale = aftersaleService.queryAfterSale(req.getOrderId());
            if (order == null || aftersale == null) {
                return AjaxResult.error("未查询到订单信息");
            }
            // 仅退款不需要退货
            if (aftersale.getType() == 1) {
                return AjaxResult.error("仅退款不需要退货");
            }
            if (aftersale.getStatus() != AftersaleStatus.WAIT.getType()) {
                return AjaxResult.error("当前状态不可退货");
            }
            // 更新退款单
            aftersale.setRefundWpCode(req.getDeliveryCompanyCode());
            aftersale.setRefundWaybillCode(req.getDeliverySn());
            aftersaleService.update(aftersale);

            return AjaxResult.success();
        } catch (Exception e) {
            log.error("用户提交退货单号异常", e);
            return AjaxResult.error("提交发货信息失败");
        } finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    @ApiOperation("批量更新订单商品图片")
    @PostMapping("/updateOrderItemImages")
    public AjaxResult updateOrderItemImages() {
        try {
            log.info("开始批量更新订单商品图片");
            orderItemService.updateOrderItemImages();
            return AjaxResult.success("订单商品图片更新成功");
        } catch (Exception e) {
            log.error("批量更新订单商品图片失败", e);
            return AjaxResult.error("订单商品图片更新失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取订单倒计时")
    @GetMapping("/countdown")
    public ResponseEntity<Map<String, Object>> getOrderCountdown(@RequestParam Long orderId) {
        try {
            long remainingSeconds = orderCountdownService.getOrderCountdownRemaining(orderId);
            boolean inCountdown = orderCountdownService.isOrderInCountdown(orderId);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("remainingSeconds", remainingSeconds);
            result.put("inCountdown", inCountdown);
            result.put("isExpired", remainingSeconds <= 0);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取订单倒计时失败", e);
            throw new RuntimeException("获取订单倒计时失败");
        }
    }

    @ApiOperation("获取多个订单倒计时")
    @PostMapping("/countdown/batch")
    public ResponseEntity<Map<String, Object>> getBatchOrderCountdown(@RequestBody List<Long> orderIds) {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<Long, Long> countdownMap = new HashMap<>();

            for (Long orderId : orderIds) {
                long remainingSeconds = orderCountdownService.getOrderCountdownRemaining(orderId);
                countdownMap.put(orderId, remainingSeconds);
            }

            result.put("countdownMap", countdownMap);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取批量订单倒计时失败", e);
            throw new RuntimeException("获取批量订单倒计时失败");
        }
    }

    @ApiOperation("检查并修复订单倒计时状态")
    @PostMapping("/countdown/check")
    public ResponseEntity<Map<String, Object>> checkOrderCountdown(@RequestParam Long orderId) {
        try {
            Map<String, Object> result = new HashMap<>();

            // 查询订单信息
            Order order = orderService.selectById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return ResponseEntity.ok(result);
            }

            // 检查订单状态
            if (!Constants.OrderStatus.NOTPAID.equals(order.getStatus())) {
                result.put("success", true);
                result.put("message", "订单状态正常，无需处理");
                return ResponseEntity.ok(result);
            }

            // 检查是否超时
            LocalDateTime createTime = order.getCreateTime();
            LocalDateTime now = LocalDateTime.now();
            long elapsedMinutes = createTime.until(now, java.time.temporal.ChronoUnit.MINUTES);

            if (elapsedMinutes >= 15) {
                // 订单已超时，应该取消
                log.info("发现超时订单 - 订单ID: {}, 创建时间: {}, 超时: {}分钟", orderId, createTime, elapsedMinutes);

                // 移除Redis倒计时
                orderCountdownService.removeOrderCountdown(orderId);

                // 取消订单
                CancelOrderForm cancelForm = new CancelOrderForm();
                cancelForm.setIdList(Arrays.asList(orderId));
                service.orderBatchCancel(cancelForm, order.getMemberId());

                result.put("success", true);
                result.put("message", "订单已超时，已自动取消");
                result.put("cancelled", true);
            } else {
                // 订单未超时，检查倒计时状态
                long remainingSeconds = orderCountdownService.getOrderCountdownRemaining(orderId);
                if (remainingSeconds <= 0) {
                    // Redis中没有倒计时数据，重新设置
                    long remainingMinutes = 15 - elapsedMinutes;
                    if (remainingMinutes > 0) {
                        orderCountdownService.setOrderCountdown(orderId, order.getOrderSn());
                        log.info("重新设置订单倒计时 - 订单ID: {}, 剩余时间: {}分钟", orderId, remainingMinutes);
                        result.put("success", true);
                        result.put("message", "已重新设置倒计时");
                        result.put("reset", true);
                    } else {
                        // 已超时，取消订单
                        CancelOrderForm cancelForm = new CancelOrderForm();
                        cancelForm.setIdList(Arrays.asList(orderId));
                        service.orderBatchCancel(cancelForm, order.getMemberId());
                        result.put("success", true);
                        result.put("message", "订单已超时，已自动取消");
                        result.put("cancelled", true);
                    }
                } else {
                    result.put("success", true);
                    result.put("message", "订单倒计时正常");
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("检查订单倒计时状态失败", e);
            throw new RuntimeException("检查订单倒计时状态失败");
        }
    }
}
