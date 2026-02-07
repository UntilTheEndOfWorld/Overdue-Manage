package com.overdue.web.controller.system;

import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.system.domain.SysConfig;
import com.overdue.system.domain.vo.SysConfigVo;
import com.overdue.system.service.ISysConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys/config")
public class ConfigController extends BaseController {

    @Autowired
    private ISysConfigService configService;

    @PreAuthorize("@ss.hasAnyRoles('admin,viewer')")
    @GetMapping(value = "/configKey2/{configKey}")
    public AjaxResult getConfigKey2(@PathVariable String configKey) {
        return AjaxResult.success(configService.selectConfigByKey2(configKey));
    }

    @PreAuthorize("@ss.hasAnyRoles(admin)")
    @PostMapping("/addOrUpdate")
    public AjaxResult addOrUpdate(@RequestBody SysConfigVo content) {
        SysConfig config = new SysConfig();
        BeanUtils.copyProperties(content,config);
        if (config.getConfigId() == null) {
            config.setCreateBy(getUserId());
            return toAjax(configService.insertConfig(config));
        }
        config.setUpdateBy(getUserId());
        return toAjax(configService.updateConfig(config));
    }
}
