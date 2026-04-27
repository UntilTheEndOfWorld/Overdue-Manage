package com.overdue.miniapp.controller;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.manager.item.domain.entity.MemberOrder;
import com.overdue.manager.item.service.MemberOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 过期了吗小程序 - 会员订单接口（/member）
 *
 * @author overdue
 */
@Api(tags = "过期了吗小程序-会员订单")
@RestController
@RequestMapping("/member")
public class MemberOrderMiniappController extends BaseMiniappController {
    private static final String MSG_ORDER_NOT_FOUND = "订单不存在";
    private static final String MSG_FORBIDDEN = "无权限";

    @Autowired
    private MemberOrderService memberOrderService;

    @ApiOperation("套餐列表（类型、名称、价格）")
    @GetMapping("/plans")
    public AjaxResult listPlans() {
        List<Map<String, Object>> list = memberOrderService.listPlanTypes();
        return AjaxResult.success(list);
    }

    @ApiOperation("创建会员订单")
    @PostMapping("/orders")
    public AjaxResult createOrder(@RequestBody Map<String, String> body) {
        return withLogin(userId -> {
            String planType = body != null ? body.get("planType") : null;
            if (planType == null || planType.isEmpty()) {
                return AjaxResult.error("请选择套餐类型");
            }
            MemberOrder order = memberOrderService.createOrder(userId, planType);
            if (order == null) {
                return AjaxResult.error("创建订单失败或套餐类型无效");
            }
            return AjaxResult.success(order);
        });
    }

    @ApiOperation("我的订单列表")
    @GetMapping("/orders")
    public AjaxResult myOrders() {
        return withLogin(userId -> AjaxResult.success(memberOrderService.selectByUserId(userId)));
    }

    @ApiOperation("订单详情")
    @GetMapping("/orders/{id}")
    public AjaxResult orderDetail(@PathVariable Long id) {
        return withLogin(userId -> {
            MemberOrder order = memberOrderService.selectById(id);
            if (order == null) {
                return AjaxResult.error(MSG_ORDER_NOT_FOUND);
            }
            if (!userId.equals(order.getUserId())) {
                return AjaxResult.error(HttpStatus.FORBIDDEN.value(), MSG_FORBIDDEN);
            }
            return AjaxResult.success(order);
        });
    }

    @ApiOperation("取消订单")
    @PutMapping("/orders/{id}/cancel")
    public AjaxResult cancel(@PathVariable Long id) {
        return withLogin(userId -> {
            boolean ok = memberOrderService.cancelOrder(id, userId);
            return ok ? AjaxResult.success() : AjaxResult.error("取消失败或订单状态不允许取消");
        });
    }
}
