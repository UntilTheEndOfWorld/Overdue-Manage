package com.overdue.manager.ums.controller;

import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.redis.RedisService;
import com.overdue.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlatformVerifyController {

    @Autowired
    private RedisService redisService;

    /**
     * h5 生成验证码
     * @param code
     * @return
     */
    @GetMapping("/h5/verified/code/generate")
    public AjaxResult createCode(String code){
        redisService.setVerifyCode(code);
        return AjaxResult.success(true);
    }


    @GetMapping("/no-auth/verified/code")
    public AjaxResult verifyCode(String code){
        String verifyCode = redisService.getVerifyCode(code);
        if (StringUtils.isEmpty(verifyCode)) {
            return AjaxResult.success(false);
        }
        redisService.deleteVerifyCode(code);
        return AjaxResult.success(true);
    }
}
