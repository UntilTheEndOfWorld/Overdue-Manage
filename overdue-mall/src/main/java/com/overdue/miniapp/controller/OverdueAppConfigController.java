package com.overdue.miniapp.controller;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.config.OverdueAppConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 过期了吗小程序 - 应用配置（无需登录）
 */
@Api(tags = "过期了吗小程序-应用配置")
@RestController
@RequestMapping("/no-auth/overdue")
public class OverdueAppConfigController {

    @Autowired
    private OverdueAppConfigService overdueAppConfigService;

    @ApiOperation("获取应用配置（是否收费、免费额度等）")
    @GetMapping("/config")
    public AjaxResult getConfig() {
        return AjaxResult.success(overdueAppConfigService.buildPublicConfig());
    }
}
