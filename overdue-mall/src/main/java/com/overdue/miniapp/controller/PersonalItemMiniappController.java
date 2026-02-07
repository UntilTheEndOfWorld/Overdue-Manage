package com.overdue.miniapp.controller;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.service.PersonalItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 过期了吗小程序 - 个人物品接口（/personal）
 *
 * @author overdue
 */
@Api(tags = "过期了吗小程序-个人物品")
@RestController
@RequestMapping("/personal")
public class PersonalItemMiniappController {

    @Autowired
    private PersonalItemService personalItemService;

    private Long getCurrentUserId() {
        Object uid = LocalDataUtil.getVar(Constants.OVERDUE_USER_ID);
        if (uid == null) {
            return null;
        }
        if (uid instanceof Long) {
            return (Long) uid;
        }
        if (uid instanceof Number) {
            return ((Number) uid).longValue();
        }
        try {
            return Long.parseLong(uid.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @ApiOperation("我的个人物品列表")
    @GetMapping("/items")
    public AjaxResult list() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        List<PersonalItem> list = personalItemService.selectByUserId(userId);
        return AjaxResult.success(list);
    }

    @ApiOperation("新增个人物品")
    @PostMapping("/items")
    public AjaxResult add(@RequestBody PersonalItem item) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        item.setUserId(userId);
        if (item.getStatus() == null) {
            item.setStatus("0");
        }
        int rows = personalItemService.insertPersonalItem(item);
        return rows > 0 ? AjaxResult.success(item) : AjaxResult.error("新增失败");
    }

    @ApiOperation("个人物品详情")
    @GetMapping("/items/{id}")
    public AjaxResult get(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        PersonalItem item = personalItemService.selectById(id);
        if (item == null) {
            return AjaxResult.error("物品不存在");
        }
        if (!userId.equals(item.getUserId())) {
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        }
        return AjaxResult.success(item);
    }

    @ApiOperation("修改个人物品")
    @PutMapping("/items/{id}")
    public AjaxResult update(@PathVariable Long id, @RequestBody PersonalItem item) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        PersonalItem existing = personalItemService.selectById(id);
        if (existing == null) {
            return AjaxResult.error("物品不存在");
        }
        if (!userId.equals(existing.getUserId())) {
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        }
        item.setId(id);
        item.setUserId(userId);
        int rows = personalItemService.updatePersonalItem(item);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("修改失败");
    }

    @ApiOperation("删除个人物品")
    @DeleteMapping("/items/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        PersonalItem existing = personalItemService.selectById(id);
        if (existing == null) {
            return AjaxResult.error("物品不存在");
        }
        if (!userId.equals(existing.getUserId())) {
            return AjaxResult.error(HttpStatus.FORBIDDEN.value(), "无权限");
        }
        int rows = personalItemService.deletePersonalItemById(id);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("删除失败");
    }
}
