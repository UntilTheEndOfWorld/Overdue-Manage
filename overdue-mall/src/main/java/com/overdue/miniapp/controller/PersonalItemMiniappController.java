package com.overdue.miniapp.controller;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.config.OverdueAppConfigService;
import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.service.OperationLogService;
import com.overdue.manager.item.service.PersonalItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.function.BiFunction;

/**
 * 过期了吗小程序 - 个人物品接口（/personal）
 *
 * @author overdue
 */
@Api(tags = "过期了吗小程序-个人物品")
@RestController
@RequestMapping("/personal")
public class PersonalItemMiniappController extends BaseMiniappController {
    private static final String MSG_ITEM_NOT_FOUND = "物品不存在";
    private static final String MSG_FORBIDDEN = "无权限";

    @Autowired
    private PersonalItemService personalItemService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private OverdueAppConfigService overdueAppConfigService;

    @ApiOperation("我的个人物品列表")
    @GetMapping("/items")
    public AjaxResult list() {
        return withLogin(userId ->
                AjaxResult.success(personalItemService.selectByUserIdWithExpireLog(userId, getCurrentUserName()))
        );
    }

    @ApiOperation("个人物品状态统计（全部/正常/即将过期/已过期）")
    @GetMapping("/items/stats")
    public AjaxResult itemStats() {
        return withLogin(userId -> AjaxResult.success(personalItemService.getItemStats(userId)));
    }

    @ApiOperation("新增个人物品")
    @PostMapping("/items")
    public AjaxResult add(@RequestBody PersonalItem item) {
        return withLogin(userId -> {
            String quotaError = overdueAppConfigService.checkPersonalItemQuota(userId);
            if (quotaError != null) {
                return AjaxResult.error(quotaError);
            }
            int rows = personalItemService.insertPersonalItemWithLog(userId, getCurrentUserName(), item);
            return rows > 0 ? AjaxResult.success(item) : AjaxResult.error("新增失败");
        });
    }

    @ApiOperation("个人物品详情")
    @GetMapping("/items/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return withOwnedItem(id, (userId, item) -> AjaxResult.success(item));
    }

    @ApiOperation("修改个人物品")
    @PutMapping("/items/{id}")
    public AjaxResult update(@PathVariable Long id, @RequestBody PersonalItem item) {
        return withOwnedItem(id, (userId, existing) -> {
            item.setId(id);
            item.setUserId(userId);
            int rows = personalItemService.updatePersonalItemWithLog(userId, getCurrentUserName(), item);
            return rows > 0 ? AjaxResult.success() : AjaxResult.error("修改失败");
        });
    }

    @ApiOperation("删除个人物品")
    @DeleteMapping("/items/{id}")
    public AjaxResult delete(@PathVariable Long id, @RequestParam(value = "opType", required = false) String opType) {
        return withOwnedItem(id, (userId, existing) -> {
            int rows = personalItemService.deletePersonalItemByIdWithLog(userId, getCurrentUserName(), id, opType);
            return rows > 0 ? AjaxResult.success() : AjaxResult.error("删除失败");
        });
    }

    @ApiOperation("个人物品操作日志")
    @GetMapping("/logs")
    public AjaxResult logs(@RequestParam(value = "itemId", required = false) Long itemId,
                           @RequestParam(value = "operationType", required = false) String operationType) {
        return withLogin(userId -> AjaxResult.success(operationLogService.selectPersonalLogs(userId, itemId, operationType)));
    }

    private AjaxResult withOwnedItem(Long itemId, BiFunction<Long, PersonalItem, AjaxResult> action) {
        return withLogin(userId -> {
            PersonalItem existing = personalItemService.selectById(itemId);
            if (existing == null) {
                return AjaxResult.error(MSG_ITEM_NOT_FOUND);
            }
            if (!userId.equals(existing.getUserId())) {
                return AjaxResult.error(HttpStatus.FORBIDDEN.value(), MSG_FORBIDDEN);
            }
            return action.apply(userId, existing);
        });
    }
}
