package com.overdue.h5.controller;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.utils.MemberSecurityUtils;
import com.overdue.manager.item.domain.form.OverdueProfileForm;
import com.overdue.manager.item.domain.vo.OverdueProfileVO;
import com.overdue.manager.item.service.OverdueUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人资料兼容接口。
 * 当 miniapp 控制器未加载时，提供 /personal/profile 给小程序调用，避免 404。
 */
@RestController
@RequestMapping("/personal/profile")
@ConditionalOnMissingBean(type = "com.overdue.miniapp.controller.PersonalProfileMiniappController")
public class PersonalProfileCompatController {

    @Autowired
    private OverdueUserService overdueUserService;

    @GetMapping
    public AjaxResult get() {
        Long userId = MemberSecurityUtils.getMemberId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), "请先登录");
        }
        OverdueProfileVO vo = overdueUserService.getProfile(userId);
        if (vo == null) {
            return AjaxResult.error("用户不存在");
        }
        return AjaxResult.success(vo);
    }

    @PutMapping
    public AjaxResult update(@RequestBody OverdueProfileForm form) {
        Long userId = MemberSecurityUtils.getMemberId();
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

