package com.overdue.miniapp.controller;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.manager.item.domain.form.OverdueProfileForm;
import com.overdue.manager.item.domain.vo.OverdueProfileVO;
import com.overdue.manager.item.service.OverdueUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 过期了吗小程序 - 个人资料
 *
 * @author overdue
 */
@Api(tags = "过期了吗小程序-个人资料")
@RestController
@RequestMapping("/personal/profile")
public class PersonalProfileMiniappController {

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

    @ApiOperation("查询个人资料")
    @GetMapping
    public AjaxResult get() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        OverdueProfileVO vo = overdueUserService.getProfile(userId);
        if (vo == null) {
            return AjaxResult.error("用户不存在");
        }
        return AjaxResult.success(vo);
    }

    @ApiOperation("更新个人资料")
    @PutMapping
    public AjaxResult update(@RequestBody OverdueProfileForm form) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        try {
            overdueUserService.updateProfile(userId, form);
            return AjaxResult.success(overdueUserService.getProfile(userId));
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
