package com.overdue.miniapp.controller;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.manager.item.domain.form.ReminderSettingsForm;
import com.overdue.manager.item.domain.vo.ReminderSettingsVO;
import com.overdue.manager.item.service.OverdueUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 过期了吗小程序 - 到期提醒设置（/personal/reminder-settings）
 *
 * @author overdue
 */
@Api(tags = "过期了吗小程序-到期提醒")
@RestController
@RequestMapping("/personal/reminder-settings")
public class PersonalReminderMiniappController {

    @Autowired
    private OverdueUserService overdueUserService;

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

    @ApiOperation("查询到期提醒设置")
    @GetMapping
    public AjaxResult get() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        ReminderSettingsVO vo = overdueUserService.getReminderSettings(userId);
        if (vo == null) {
            return AjaxResult.error("用户不存在");
        }
        return AjaxResult.success(vo);
    }

    @ApiOperation("更新到期提醒设置")
    @PutMapping
    public AjaxResult update(@RequestBody ReminderSettingsForm form) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        try {
            overdueUserService.updateReminderSettings(userId, form);
            return AjaxResult.success(overdueUserService.getReminderSettings(userId));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
