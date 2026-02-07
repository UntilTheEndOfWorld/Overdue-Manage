package com.overdue.web.controller.item;

import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.manager.item.domain.entity.*;
import com.overdue.manager.item.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 过期了吗-数据统计Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-数据统计")
@RestController
@RequestMapping("/item/statistics")
public class OverdueStatisticsController extends BaseController {

    @Autowired
    private OverdueUserService overdueUserService;

    @Autowired
    private PersonalItemService personalItemService;

    @Autowired
    private SharedSpaceService sharedSpaceService;

    @Autowired
    private SharedItemService sharedItemService;

    @Autowired
    private OverdueMemberService overdueMemberService;

    @Autowired
    private MemberOrderService memberOrderService;

    @Autowired
    private AdWatchRecordService adWatchRecordService;

    /**
     * 获取总览统计数据
     */
    @ApiOperation("获取总览统计数据")
    @PreAuthorize("@ss.hasPermi('item:statistics:list')")
    @GetMapping("/overview")
    public AjaxResult getOverview() {
        Map<String, Object> data = new HashMap<>();

        // 用户统计
        List<OverdueUser> users = overdueUserService.selectOverdueUserList(new OverdueUser());
        data.put("totalUsers", users != null ? users.size() : 0);

        // 个人物品统计
        List<PersonalItem> personalItems = personalItemService.selectPersonalItemList(new PersonalItem());
        data.put("totalPersonalItems", personalItems != null ? personalItems.size() : 0);

        // 共享空间统计
        List<SharedSpace> sharedSpaces = sharedSpaceService.selectSharedSpaceList(new SharedSpace());
        data.put("totalSharedSpaces", sharedSpaces != null ? sharedSpaces.size() : 0);

        // 共享物品统计
        List<SharedItem> sharedItems = sharedItemService.selectSharedItemList(new SharedItem());
        data.put("totalSharedItems", sharedItems != null ? sharedItems.size() : 0);

        // 会员统计
        List<OverdueMember> members = overdueMemberService.selectOverdueMemberList(new OverdueMember());
        int totalMembers = members != null ? members.size() : 0;
        int activeMembers = 0;
        if (members != null) {
            for (OverdueMember m : members) {
                if (m.getIsMember() != null && m.getIsMember() == 1) {
                    activeMembers++;
                }
            }
        }
        data.put("totalMembers", totalMembers);
        data.put("activeMembers", activeMembers);

        // 订单统计
        List<MemberOrder> orders = memberOrderService.selectMemberOrderList(new MemberOrder());
        int totalOrders = orders != null ? orders.size() : 0;
        int paidOrders = 0;
        double totalRevenue = 0;
        if (orders != null) {
            for (MemberOrder o : orders) {
                if ("1".equals(o.getStatus())) { // 已支付
                    paidOrders++;
                    if (o.getPrice() != null) {
                        totalRevenue += o.getPrice().doubleValue();
                    }
                }
            }
        }
        data.put("totalOrders", totalOrders);
        data.put("paidOrders", paidOrders);
        data.put("totalRevenue", totalRevenue);

        // 广告统计
        Map<String, Object> adStats = adWatchRecordService.getStatistics();
        data.put("adWatchStats", adStats);

        return AjaxResult.success(data);
    }

    /**
     * 获取用户增长趋势（按日期）
     */
    @ApiOperation("获取用户增长趋势")
    @PreAuthorize("@ss.hasPermi('item:statistics:list')")
    @GetMapping("/user-trend")
    public AjaxResult getUserTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // 简化实现：返回用户列表，前端可按创建时间分组统计
        List<OverdueUser> users = overdueUserService.selectOverdueUserList(new OverdueUser());
        return AjaxResult.success(users);
    }

    /**
     * 获取物品过期统计
     */
    @ApiOperation("获取物品过期统计")
    @PreAuthorize("@ss.hasPermi('item:statistics:list')")
    @GetMapping("/expiry-stats")
    public AjaxResult getExpiryStats() {
        Map<String, Object> data = new HashMap<>();

        // 获取所有个人物品进行分类统计
        List<PersonalItem> personalItems = personalItemService.selectPersonalItemList(new PersonalItem());
        int personalTotal = personalItems != null ? personalItems.size() : 0;
        int personalExpired = 0;
        int personalExpiringSoon = 0; // 7天内
        int personalNormal = 0;

        if (personalItems != null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate sevenDaysLater = now.plusDays(7);
            for (PersonalItem item : personalItems) {
                if (item.getExpiryDate() != null) {
                    java.time.LocalDate expiry = item.getExpiryDate();
                    if (expiry.isBefore(now)) {
                        personalExpired++;
                    } else if (!expiry.isAfter(sevenDaysLater)) {
                        personalExpiringSoon++;
                    } else {
                        personalNormal++;
                    }
                } else {
                    personalNormal++;
                }
            }
        }

        Map<String, Object> personalStats = new HashMap<>();
        personalStats.put("total", personalTotal);
        personalStats.put("expired", personalExpired);
        personalStats.put("expiringSoon", personalExpiringSoon);
        personalStats.put("normal", personalNormal);
        data.put("personalItems", personalStats);

        // 获取所有共享物品进行分类统计
        List<SharedItem> sharedItems = sharedItemService.selectSharedItemList(new SharedItem());
        int sharedTotal = sharedItems != null ? sharedItems.size() : 0;
        int sharedExpired = 0;
        int sharedExpiringSoon = 0;
        int sharedNormal = 0;

        if (sharedItems != null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate sevenDaysLater = now.plusDays(7);
            for (SharedItem item : sharedItems) {
                if (item.getExpiryDate() != null) {
                    java.time.LocalDate expiry = item.getExpiryDate();
                    if (expiry.isBefore(now)) {
                        sharedExpired++;
                    } else if (!expiry.isAfter(sevenDaysLater)) {
                        sharedExpiringSoon++;
                    } else {
                        sharedNormal++;
                    }
                } else {
                    sharedNormal++;
                }
            }
        }

        Map<String, Object> sharedStats = new HashMap<>();
        sharedStats.put("total", sharedTotal);
        sharedStats.put("expired", sharedExpired);
        sharedStats.put("expiringSoon", sharedExpiringSoon);
        sharedStats.put("normal", sharedNormal);
        data.put("sharedItems", sharedStats);

        return AjaxResult.success(data);
    }

    /**
     * 获取订单统计（按套餐类型）
     */
    @ApiOperation("获取订单统计（按套餐类型）")
    @PreAuthorize("@ss.hasPermi('item:statistics:list')")
    @GetMapping("/order-stats")
    public AjaxResult getOrderStats() {
        Map<String, Object> data = new HashMap<>();
        List<MemberOrder> orders = memberOrderService.selectMemberOrderList(new MemberOrder());

        Map<String, Integer> byPlanType = new HashMap<>();
        Map<String, Integer> byStatus = new HashMap<>();
        double totalRevenue = 0;

        if (orders != null) {
            for (MemberOrder order : orders) {
                // 按套餐类型统计
                String planType = order.getPlanType() != null ? order.getPlanType() : "unknown";
                byPlanType.put(planType, byPlanType.getOrDefault(planType, 0) + 1);

                // 按状态统计
                String status = order.getStatus() != null ? order.getStatus() : "unknown";
                String statusName;
                switch (status) {
                    case "0":
                        statusName = "待支付";
                        break;
                    case "1":
                        statusName = "已支付";
                        break;
                    case "2":
                        statusName = "已取消";
                        break;
                    default:
                        statusName = "未知";
                        break;
                }
                byStatus.put(statusName, byStatus.getOrDefault(statusName, 0) + 1);

                // 统计收入（仅已支付）
                if ("1".equals(status) && order.getPrice() != null) {
                    totalRevenue += order.getPrice().doubleValue();
                }
            }
        }

        data.put("byPlanType", byPlanType);
        data.put("byStatus", byStatus);
        data.put("totalRevenue", totalRevenue);
        data.put("totalOrders", orders != null ? orders.size() : 0);

        return AjaxResult.success(data);
    }

    /**
     * 获取会员统计
     */
    @ApiOperation("获取会员统计")
    @PreAuthorize("@ss.hasPermi('item:statistics:list')")
    @GetMapping("/member-stats")
    public AjaxResult getMemberStats() {
        Map<String, Object> data = new HashMap<>();
        List<OverdueMember> members = overdueMemberService.selectOverdueMemberList(new OverdueMember());

        int total = members != null ? members.size() : 0;
        int active = 0;
        int expired = 0;
        Map<String, Integer> byPlanType = new HashMap<>();

        if (members != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (OverdueMember m : members) {
                if (m.getIsMember() != null && m.getIsMember() == 1) {
                    if (m.getExpireTime() != null && m.getExpireTime().isAfter(now)) {
                        active++;
                    } else {
                        expired++;
                    }
                }
                String planType = m.getPlanType() != null ? m.getPlanType() : "none";
                byPlanType.put(planType, byPlanType.getOrDefault(planType, 0) + 1);
            }
        }

        data.put("total", total);
        data.put("active", active);
        data.put("expired", expired);
        data.put("byPlanType", byPlanType);

        return AjaxResult.success(data);
    }
}
