package com.overdue.miniapp.controller;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
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
import java.util.UUID;

/**
 * 过期了吗小程序 - 共享空间及共享物品、邀请、日志接口（/shared）
 *
 * @author overdue
 */
@Api(tags = "过期了吗小程序-共享空间")
@RestController
@RequestMapping("/shared")
public class SharedSpaceMiniappController {

    @Autowired
    private SharedSpaceService sharedSpaceService;
    @Autowired
    private SpaceMemberService spaceMemberService;
    @Autowired
    private SharedItemService sharedItemService;
    @Autowired
    private OperationLogService operationLogService;

    private Long getCurrentUserId() {
        Object uid = LocalDataUtil.getVar(Constants.OVERDUE_USER_ID);
        if (uid == null)
            return null;
        if (uid instanceof Long)
            return (Long) uid;
        if (uid instanceof Number)
            return ((Number) uid).longValue();
        try {
            return Long.parseLong(uid.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isMember(Long spaceId, Long userId) {
        return spaceId != null && userId != null && spaceMemberService.isMember(spaceId, userId);
    }

    @ApiOperation("我参与的空间列表")
    @GetMapping("/spaces")
    public AjaxResult listSpaces() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        List<SharedSpace> list = sharedSpaceService.selectByUserId(userId);
        return AjaxResult.success(list);
    }

    @ApiOperation("创建共享空间")
    @PostMapping("/spaces")
    public AjaxResult createSpace(@RequestBody SharedSpace space) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        space.setCreatorId(userId);
        if (space.getStatus() == null)
            space.setStatus("0");
        int rows = sharedSpaceService.insertSharedSpace(space);
        if (rows <= 0)
            return AjaxResult.error("创建失败");
        spaceMemberService.addMember(space.getId(), userId, "creator");
        return AjaxResult.success(space);
    }

    @ApiOperation("空间详情")
    @GetMapping("/spaces/{id}")
    public AjaxResult getSpace(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        SharedSpace space = sharedSpaceService.selectById(id);
        if (space == null)
            return AjaxResult.error("空间不存在");
        if (!isMember(id, userId))
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        return AjaxResult.success(space);
    }

    @ApiOperation("修改空间")
    @PutMapping("/spaces/{id}")
    public AjaxResult updateSpace(@PathVariable Long id, @RequestBody SharedSpace space) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        SharedSpace existing = sharedSpaceService.selectById(id);
        if (existing == null)
            return AjaxResult.error("空间不存在");
        if (!existing.getCreatorId().equals(userId)) {
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "仅创建者可修改");
        }
        space.setId(id);
        int rows = sharedSpaceService.updateSharedSpace(space);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("修改失败");
    }

    @ApiOperation("生成/刷新邀请码")
    @PostMapping("/spaces/{id}/invite")
    public AjaxResult generateInvite(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        SharedSpace space = sharedSpaceService.selectById(id);
        if (space == null)
            return AjaxResult.error("空间不存在");
        if (!isMember(id, userId))
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        space.setInviteCode(code);
        space.setInviteCodeExpireTime(LocalDateTime.now().plusDays(7));
        sharedSpaceService.updateSharedSpace(space);
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("inviteCode", code);
        return AjaxResult.success(data);
    }

    @ApiOperation("通过邀请码加入空间")
    @PostMapping("/spaces/join")
    public AjaxResult joinSpace(@RequestBody java.util.Map<String, String> body) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        String inviteCode = body != null ? body.get("inviteCode") : null;
        if (inviteCode == null || inviteCode.isEmpty()) {
            return AjaxResult.error("邀请码不能为空");
        }
        SharedSpace space = sharedSpaceService.selectByInviteCode(inviteCode);
        if (space == null)
            return AjaxResult.error("邀请码无效或已过期");
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
        info.put("spaceId", space.getId());
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
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        boolean isMember = spaceMemberService.isMember(spaceId, userId);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("isMember", isMember);
        result.put("spaceId", spaceId);
        result.put("userId", userId);
        return AjaxResult.success(result);
    }

    /**
     * 退出空间
     */
    @ApiOperation("退出空间")
    @DeleteMapping("/spaces/{spaceId}/leave")
    public AjaxResult leaveSpace(@PathVariable Long spaceId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        SharedSpace space = sharedSpaceService.selectById(spaceId);
        if (space == null) {
            return AjaxResult.error("空间不存在");
        }
        // 创建者不能退出
        if (space.getCreatorId().equals(userId)) {
            return AjaxResult.error("创建者不能退出空间，请先转让或解散");
        }
        if (!spaceMemberService.isMember(spaceId, userId)) {
            return AjaxResult.error("您不是该空间成员");
        }
        int rows = spaceMemberService.leaveSpace(spaceId, userId);
        return rows > 0 ? AjaxResult.success("已退出空间") : AjaxResult.error("退出失败");
    }

    /**
     * 获取空间成员列表
     */
    @ApiOperation("获取空间成员列表")
    @GetMapping("/spaces/{spaceId}/members")
    public AjaxResult listMembers(@PathVariable Long spaceId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        if (!isMember(spaceId, userId)) {
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        }
        return AjaxResult.success(spaceMemberService.selectBySpaceId(spaceId));
    }

    @ApiOperation("空间内物品列表")
    @GetMapping("/spaces/{spaceId}/items")
    public AjaxResult listItems(@PathVariable Long spaceId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        if (!isMember(spaceId, userId))
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        List<SharedItem> list = sharedItemService.selectBySpaceId(spaceId);
        return AjaxResult.success(list);
    }

    @ApiOperation("新增共享物品")
    @PostMapping("/spaces/{spaceId}/items")
    public AjaxResult addItem(@PathVariable Long spaceId, @RequestBody SharedItem item) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        if (!isMember(spaceId, userId))
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        item.setSpaceId(spaceId);
        item.setCreatorId(userId);
        if (item.getStatus() == null)
            item.setStatus("0");
        int rows = sharedItemService.insertSharedItem(item);
        return rows > 0 ? AjaxResult.success(item) : AjaxResult.error("新增失败");
    }

    @ApiOperation("共享物品详情")
    @GetMapping("/spaces/{spaceId}/items/{itemId}")
    public AjaxResult getItem(@PathVariable Long spaceId, @PathVariable Long itemId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        if (!isMember(spaceId, userId))
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        SharedItem item = sharedItemService.selectById(itemId);
        if (item == null || !spaceId.equals(item.getSpaceId()))
            return AjaxResult.error("物品不存在");
        return AjaxResult.success(item);
    }

    @ApiOperation("修改共享物品")
    @PutMapping("/spaces/{spaceId}/items/{itemId}")
    public AjaxResult updateItem(@PathVariable Long spaceId, @PathVariable Long itemId, @RequestBody SharedItem item) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        if (!isMember(spaceId, userId))
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        SharedItem existing = sharedItemService.selectById(itemId);
        if (existing == null || !spaceId.equals(existing.getSpaceId()))
            return AjaxResult.error("物品不存在");
        item.setId(itemId);
        item.setSpaceId(spaceId);
        item.setCreatorId(existing.getCreatorId());
        int rows = sharedItemService.updateSharedItem(item);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("修改失败");
    }

    @ApiOperation("删除共享物品")
    @DeleteMapping("/spaces/{spaceId}/items/{itemId}")
    public AjaxResult deleteItem(@PathVariable Long spaceId, @PathVariable Long itemId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        if (!isMember(spaceId, userId))
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        SharedItem existing = sharedItemService.selectById(itemId);
        if (existing == null || !spaceId.equals(existing.getSpaceId()))
            return AjaxResult.error("物品不存在");
        int rows = sharedItemService.deleteSharedItemById(itemId);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    @ApiOperation("空间操作日志")
    @GetMapping("/spaces/{spaceId}/logs")
    public AjaxResult logs(@PathVariable Long spaceId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        if (!isMember(spaceId, userId))
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        List<OperationLog> list = operationLogService.selectBySpaceId(spaceId);
        return AjaxResult.success(list);
    }
}
