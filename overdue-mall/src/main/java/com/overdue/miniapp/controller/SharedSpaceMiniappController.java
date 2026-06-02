package com.overdue.miniapp.controller;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.config.OverdueAppConfigService;
import com.overdue.manager.item.domain.entity.OperationLog;
import com.overdue.manager.item.domain.entity.SharedItem;
import com.overdue.manager.item.domain.entity.SharedSpace;
import com.overdue.manager.item.service.OperationLogService;
import com.overdue.manager.item.service.SharedItemService;
import com.overdue.manager.item.service.SharedSpaceService;
import com.overdue.manager.item.service.SpaceMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.UUID;

/**
 * 过期了吗小程序 - 共享空间及共享物品、邀请、日志接口（/shared）
 *
 * @author overdue
 */
@Api(tags = "过期了吗小程序-共享空间")
@RestController
@RequestMapping("/shared")
public class SharedSpaceMiniappController extends BaseMiniappController {
    private static final String MSG_SPACE_NOT_FOUND = "空间不存在";
    private static final String MSG_ITEM_NOT_FOUND = "物品不存在";
    private static final String MSG_FORBIDDEN = "无权限";

    @Autowired
    private SharedSpaceService sharedSpaceService;
    @Autowired
    private SpaceMemberService spaceMemberService;
    @Autowired
    private SharedItemService sharedItemService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private OverdueAppConfigService overdueAppConfigService;

    private boolean isMember(Long spaceId, Long userId) {
        return spaceId != null && userId != null && spaceMemberService.isMember(spaceId, userId);
    }

    private void saveOperationLog(Long spaceId, Long userId, String userName, String operationType, SharedItem item, String operationDesc, String operationData) {
        if (spaceId == null || userId == null || item == null || item.getId() == null) {
            return;
        }
        OperationLog log = new OperationLog();
        log.setSpaceId(spaceId);
        log.setItemId(item.getId());
        log.setItemType("shared");
        log.setOperatorId(userId);
        log.setOperatorName(userName);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setOperationData(operationData);
        log.setOperationTime(LocalDateTime.now());
        operationLogService.insertOperationLog(log);
    }

    @ApiOperation("我参与的空间列表")
    @GetMapping("/spaces")
    public AjaxResult listSpaces() {
        return withLogin(userId -> AjaxResult.success(sharedSpaceService.selectByUserId(userId)));
    }

    @ApiOperation("创建共享空间")
    @PostMapping("/spaces")
    public AjaxResult createSpace(@RequestBody SharedSpace space) {
        return withLogin(userId -> {
            String quotaError = overdueAppConfigService.checkSharedSpaceQuota(userId);
            if (quotaError != null) {
                return AjaxResult.error(quotaError);
            }
            space.setCreatorId(userId);
            if (space.getStatus() == null) {
                space.setStatus("0");
            }
            int rows = sharedSpaceService.insertSharedSpace(space);
            if (rows <= 0) {
                return AjaxResult.error("创建失败");
            }
            spaceMemberService.addMember(space.getId(), userId, "creator");
            return AjaxResult.success(space);
        });
    }

    @ApiOperation("空间详情")
    @GetMapping("/spaces/{id}")
    public AjaxResult getSpace(@PathVariable Long id) {
        return withSpaceMember(id, (userId, space) -> AjaxResult.success(space));
    }

    @ApiOperation("修改空间")
    @PutMapping("/spaces/{id}")
    public AjaxResult updateSpace(@PathVariable Long id, @RequestBody SharedSpace space) {
        return withLogin(userId -> {
            SharedSpace existing = sharedSpaceService.selectById(id);
            if (existing == null) {
                return AjaxResult.error(MSG_SPACE_NOT_FOUND);
            }
            if (!userId.equals(existing.getCreatorId())) {
                return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "仅创建者可修改");
            }
            space.setId(id);
            int rows = sharedSpaceService.updateSharedSpace(space);
            return rows > 0 ? AjaxResult.success() : AjaxResult.error("修改失败");
        });
    }

    @ApiOperation("生成/刷新邀请码")
    @PostMapping("/spaces/{id}/invite")
    public AjaxResult generateInvite(@PathVariable Long id) {
        return withSpaceMember(id, (userId, space) -> {
            String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            space.setInviteCode(code);
            space.setInviteCodeExpireTime(LocalDateTime.now().plusDays(7));
            sharedSpaceService.updateSharedSpace(space);
            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("inviteCode", code);
            return AjaxResult.success(data);
        });
    }

    @ApiOperation("通过邀请码加入空间")
    @PostMapping("/spaces/join")
    public AjaxResult joinSpace(@RequestBody java.util.Map<String, String> body) {
        return withLogin(userId -> {
            String inviteCode = body != null ? body.get("inviteCode") : null;
            if (inviteCode == null || inviteCode.isEmpty()) {
                return AjaxResult.error("邀请码不能为空");
            }
            SharedSpace space = sharedSpaceService.selectByInviteCode(inviteCode);
            if (space == null) {
                return AjaxResult.error("邀请码无效或已过期");
            }
            if (space.getInviteCodeExpireTime() != null && space.getInviteCodeExpireTime().isBefore(LocalDateTime.now())) {
                return AjaxResult.error("邀请码已过期");
            }
            boolean alreadyMember = spaceMemberService.isMember(space.getId(), userId);
            if (!alreadyMember) {
                spaceMemberService.addMember(space.getId(), userId, "member");
            }
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("space", space);
            result.put("alreadyMember", alreadyMember);
            result.put("joined", !alreadyMember);
            return AjaxResult.success(result);
        });
    }

    /**
     * 公开接口：根据邀请码获取空间基本信息（无需登录）
     * 用于邀请链接页面预览空间信息
     */
    @ApiOperation("根据邀请码获取空间信息（公开接口）")
    @GetMapping("/no-auth/invite-info")
    public AjaxResult getInviteInfo(@RequestParam String inviteCode) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return AjaxResult.error("邀请码不能为空");
        }
        SharedSpace space = sharedSpaceService.selectByInviteCode(inviteCode.trim());
        if (space == null) {
            return AjaxResult.error("邀请码无效");
        }
        // 检查邀请码是否过期
        boolean expired = space.getInviteCodeExpireTime() != null
                && space.getInviteCodeExpireTime().isBefore(LocalDateTime.now());
        if (expired) {
            return AjaxResult.error("邀请码已过期");
        }
        // 返回基本信息（不含敏感数据）
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        // 小程序端 number 无法安全表示雪花 ID，使用字符串
        info.put("spaceId", space.getId() != null ? String.valueOf(space.getId()) : null);
        info.put("spaceName", space.getName());
        info.put("description", space.getDescription());
        info.put("inviteCode", inviteCode);
        info.put("valid", true);
        return AjaxResult.success(info);
    }

    /**
     * 检查当前用户是否是某空间成员（需登录）
     */
    @ApiOperation("检查是否是空间成员")
    @GetMapping("/spaces/{spaceId}/check-member")
    public AjaxResult checkMember(@PathVariable Long spaceId) {
        return withLogin(userId -> {
            boolean isMember = spaceMemberService.isMember(spaceId, userId);
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("isMember", isMember);
            result.put("spaceId", spaceId);
            result.put("userId", userId);
            return AjaxResult.success(result);
        });
    }

    /**
     * 退出空间
     */
    @ApiOperation("退出空间")
    @DeleteMapping("/spaces/{spaceId}/leave")
    public AjaxResult leaveSpace(@PathVariable Long spaceId) {
        return withLogin(userId -> {
            SharedSpace space = sharedSpaceService.selectById(spaceId);
            if (space == null) {
                return AjaxResult.error(MSG_SPACE_NOT_FOUND);
            }
            if (userId.equals(space.getCreatorId())) {
                return AjaxResult.error("创建者不能退出空间，请先转让或解散");
            }
            if (!spaceMemberService.isMember(spaceId, userId)) {
                return AjaxResult.error("您不是该空间成员");
            }
            int rows = spaceMemberService.leaveSpace(spaceId, userId);
            return rows > 0 ? AjaxResult.success("已退出空间") : AjaxResult.error("退出失败");
        });
    }

    /**
     * 获取空间成员列表
     */
    @ApiOperation("获取空间成员列表")
    @GetMapping("/spaces/{spaceId}/members")
    public AjaxResult listMembers(@PathVariable Long spaceId) {
        return withSpaceMember(spaceId, (userId, space) -> AjaxResult.success(spaceMemberService.selectBySpaceId(spaceId)));
    }

    @ApiOperation("空间内物品列表")
    @GetMapping("/spaces/{spaceId}/items")
    public AjaxResult listItems(@PathVariable Long spaceId) {
        return withSpaceMember(spaceId, (userId, space) -> AjaxResult.success(sharedItemService.selectBySpaceId(spaceId)));
    }

    @ApiOperation("新增共享物品")
    @PostMapping("/spaces/{spaceId}/items")
    public AjaxResult addItem(@PathVariable Long spaceId, @RequestBody SharedItem item) {
        return withSpaceMember(spaceId, (userId, space) -> {
            String quotaError = overdueAppConfigService.checkSharedItemQuota(spaceId);
            if (quotaError != null) {
                return AjaxResult.error(quotaError);
            }
            item.setSpaceId(spaceId);
            item.setCreatorId(userId);
            if (item.getStatus() == null) {
                item.setStatus("0");
            }
            int rows = sharedItemService.insertSharedItem(item);
            if (rows > 0) {
                String operationData = "{\"category\":\"" + (item.getCategory() == null ? "" : item.getCategory())
                        + "\",\"expiryDate\":\"" + (item.getExpiryDate() == null ? "" : item.getExpiryDate()) + "\"}";
                saveOperationLog(spaceId, userId, getCurrentUserName(), "add", item, "添加了物品\"" + item.getName() + "\"", operationData);
            }
            return rows > 0 ? AjaxResult.success(item) : AjaxResult.error("新增失败");
        });
    }

    @ApiOperation("共享物品详情")
    @GetMapping("/spaces/{spaceId}/items/{itemId}")
    public AjaxResult getItem(@PathVariable Long spaceId, @PathVariable Long itemId) {
        return withSpaceMember(spaceId, (userId, space) -> {
            SharedItem item = sharedItemService.selectById(itemId);
            if (item == null || !spaceId.equals(item.getSpaceId())) {
                return AjaxResult.error(MSG_ITEM_NOT_FOUND);
            }
            return AjaxResult.success(item);
        });
    }

    @ApiOperation("修改共享物品")
    @PutMapping("/spaces/{spaceId}/items/{itemId}")
    public AjaxResult updateItem(@PathVariable Long spaceId, @PathVariable Long itemId, @RequestBody SharedItem item) {
        return withSpaceMember(spaceId, (userId, space) -> {
            SharedItem existing = sharedItemService.selectById(itemId);
            if (existing == null || !spaceId.equals(existing.getSpaceId())) {
                return AjaxResult.error(MSG_ITEM_NOT_FOUND);
            }
            SharedItem before = sharedItemService.selectById(itemId);
            item.setId(itemId);
            item.setSpaceId(spaceId);
            item.setCreatorId(existing.getCreatorId());
            int rows = sharedItemService.updateSharedItem(item);
            if (rows > 0) {
                String operationData = "{\"name\":\"" + (item.getName() == null ? "" : item.getName())
                        + "\",\"category\":\"" + (item.getCategory() == null ? "" : item.getCategory())
                        + "\",\"expiryDate\":\"" + (item.getExpiryDate() == null ? "" : item.getExpiryDate())
                        + "\",\"beforeExpiryDate\":\"" + (before == null || before.getExpiryDate() == null ? "" : before.getExpiryDate()) + "\"}";
                saveOperationLog(spaceId, userId, getCurrentUserName(), "update", item, "更新了物品\"" + item.getName() + "\"", operationData);
            }
            return rows > 0 ? AjaxResult.success() : AjaxResult.error("修改失败");
        });
    }

    @ApiOperation("删除共享物品")
    @DeleteMapping("/spaces/{spaceId}/items/{itemId}")
    public AjaxResult deleteItem(@PathVariable Long spaceId, @PathVariable Long itemId,
                                 @RequestParam(value = "opType", required = false) String opType) {
        return withSpaceMember(spaceId, (userId, space) -> {
            SharedItem existing = sharedItemService.selectById(itemId);
            if (existing == null || !spaceId.equals(existing.getSpaceId())) {
                return AjaxResult.error(MSG_ITEM_NOT_FOUND);
            }
            int rows = sharedItemService.deleteSharedItemById(itemId);
            if (rows > 0) {
                String operationType = "process".equalsIgnoreCase(opType) ? "process" : "delete";
                String operationDesc = "process".equalsIgnoreCase(opType)
                        ? "处理了过期物品\"" + existing.getName() + "\""
                        : "删除了物品\"" + existing.getName() + "\"";
                String operationData = "{\"source\":\"" + operationType + "\",\"status\":\"1\"}";
                saveOperationLog(spaceId, userId, getCurrentUserName(), operationType, existing, operationDesc, operationData);
            }
            return rows > 0 ? AjaxResult.success() : AjaxResult.error("删除失败");
        });
    }

    @ApiOperation("空间操作日志")
    @GetMapping("/spaces/{spaceId}/logs")
    public AjaxResult logs(@PathVariable Long spaceId) {
        return withSpaceMember(spaceId, (userId, space) -> AjaxResult.success(operationLogService.selectBySpaceId(spaceId)));
    }

    private AjaxResult withSpaceMember(Long spaceId, BiFunction<Long, SharedSpace, AjaxResult> action) {
        return withLogin(userId -> {
            SharedSpace space = sharedSpaceService.selectById(spaceId);
            if (space == null) {
                return AjaxResult.error(MSG_SPACE_NOT_FOUND);
            }
            if (!isMember(spaceId, userId)) {
                return AjaxResult.error(HttpStatus.FORBIDDEN.value(), MSG_FORBIDDEN);
            }
            return action.apply(userId, space);
        });
    }
}
