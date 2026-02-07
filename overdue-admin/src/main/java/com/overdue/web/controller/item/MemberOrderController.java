package com.overdue.web.controller.item;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.item.domain.entity.MemberOrder;
import com.overdue.manager.item.service.MemberOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 过期了吗 - 会员订单管理（后台）
 *
 * @author overdue
 */
@Api(tags = "过期了吗-会员订单管理")
@RestController
@RequestMapping("/item/order")
public class MemberOrderController extends BaseController {

    @Autowired
    private MemberOrderService memberOrderService;

    @ApiOperation("会员订单列表")
    @PreAuthorize("@ss.hasPermi('item:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(MemberOrder query) {
        startPage();
        List<MemberOrder> list = memberOrderService.selectMemberOrderList(query);
        return getDataTable(list);
    }

    @ApiOperation("会员订单详情")
    @PreAuthorize("@ss.hasPermi('item:order:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        MemberOrder order = memberOrderService.selectById(id);
        return order != null ? AjaxResult.success(order) : AjaxResult.error("订单不存在");
    }

    @ApiOperation("取消订单")
    @PreAuthorize("@ss.hasPermi('item:order:edit')")
    @Log(title = "会员订单", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable Long id) {
        boolean ok = memberOrderService.cancelOrder(id, null);
        return ok ? AjaxResult.success() : AjaxResult.error("取消失败或订单状态不允许取消");
    }
}
