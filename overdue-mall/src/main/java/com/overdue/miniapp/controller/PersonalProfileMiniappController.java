package com.overdue.miniapp.controller;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.manager.item.domain.form.OverdueProfileForm;
import com.overdue.manager.item.domain.vo.OverdueProfileVO;
import com.overdue.manager.item.service.OverdueUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 过期了吗小程序 - 个人资料
 *
 * @author overdue
 */
@Api(tags = "过期了吗小程序-个人资料")
@RestController
@RequestMapping("/personal/profile")
public class PersonalProfileMiniappController extends BaseMiniappController {

    @Autowired
    private OverdueUserService overdueUserService;

    @ApiOperation("查询个人资料")
    @GetMapping
    public AjaxResult get() {
        return withLogin(userId -> {
            OverdueProfileVO vo = overdueUserService.getProfile(userId);
            if (vo == null) {
                return AjaxResult.error("用户不存在");
            }
            return AjaxResult.success(vo);
        });
    }

    @ApiOperation("更新个人资料")
    @PutMapping
    public AjaxResult update(@RequestBody OverdueProfileForm form) {
        return withLogin(userId -> {
            try {
                overdueUserService.updateProfile(userId, form);
                return AjaxResult.success(overdueUserService.getProfile(userId));
            } catch (IllegalArgumentException e) {
                return AjaxResult.error(e.getMessage());
            }
        });
    }
}
