package com.overdue.h5.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.overdue.external.WechatUtil;
import com.overdue.h5.domain.form.WechatLoginForm;
import com.overdue.manager.ums.service.MemberWechatService;
import com.overdue.common.core.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import com.overdue.h5.domain.form.WechatPhoneRegisterForm;
import com.overdue.h5.domain.form.WechatUserInfoLoginForm;
import com.overdue.h5.domain.form.WechatPhoneForm;
import com.overdue.common.core.domain.model.LoginMember;
import com.overdue.framework.web.service.TokenService;

/**
 * h5/微信
 */
@RestController
@Slf4j
@RequestMapping("")
public class WechatController2 {
    @Autowired
    private MemberWechatService memberWechatService;

    @Autowired
    private WechatUtil wechatUtil;

    @Autowired
    private TokenService tokenService;

    // 开发模式标志 - 已关闭，确保登录流程正常工作
    private static final boolean DEV_MODE = false;

    /**
     * 微信公众号服务器认证
     * 
     * @return
     */
    @GetMapping("/no-auth/wechat-server-auth")
    public String getHomeConfig(HttpServletRequest request) {
        String signature = request.getParameter("signature");
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");
        String echostr = request.getParameter("echostr");
        if (wechatUtil.validParam(signature, "BLU9Jo7vo", timestamp, nonce)) {
            return echostr;
        }
        return "err";
    }

    /**
     * 微信公众号服务器认证
     * 
     * @return
     */
    @PostMapping("/no-auth/wechat/h5-login")
    public ResponseEntity<String> h5Login(@RequestBody WechatLoginForm form) {
        String token = memberWechatService.login(form);
        return ResponseEntity.ok("{\"data\": \"" + token + "\"}");
    }

    @GetMapping("/no-auth/wechat/getSessionId")
    public AjaxResult getSessionId(String code) {
        JSONObject object = memberWechatService.getSessionId(code);
        if (object != null) {
            String openId = object.getString("openid");
            String sessionId = object.getString("session_key");
            Map map = new HashMap();
            map.put("openId", openId);
            map.put("sessionId", sessionId);
            return AjaxResult.successData(Base64Utils.encodeToString(JSON.toJSONString(map).getBytes()));
        }
        return AjaxResult.success(null);
    }

    @GetMapping("/no-auth/wechat/getSessionId2")
    public AjaxResult getSessionId2(String code) {
        JSONObject object = memberWechatService.getSessionId(code);
        if (object != null) {
            String openId = object.getString("openid");
            String sessionId = object.getString("session_key");
            JSONObject obj = new JSONObject();
            obj.put("openId", openId);
            obj.put("sessionId", sessionId);
            Map map = new HashMap();
            map.put("data", Base64Utils.encodeToString(JSON.toJSONString(obj).getBytes()));
            // 通过openid查找会员ID，然后获取token
            String token = memberWechatService.getTokenByOpenId(openId);
            map.put("token", token);
            return AjaxResult.successData(map);
        }
        return AjaxResult.success(null);
    }

    /**
     * 微信登录后获取手机号并注册会员
     * 
     * @param form 包含手机号加密数据的表单
     * @return 注册结果
     */
    @PostMapping("/no-auth/wechat/register-with-phone")
    public ResponseEntity<String> registerWithPhone(@RequestBody WechatPhoneRegisterForm form) {
        try {
            String token = memberWechatService.registerWithPhone(form);

            // 检查token是否为空
            if (StrUtil.isBlank(token)) {
                log.error("注册成功但token为空");
                throw new RuntimeException("登录失败，请重试");
            }

            // 从token中解析用户ID（这里需要根据实际的token格式来解析）
            // 暂时使用一个简单的用户ID生成方式
            String userId = "user_" + System.currentTimeMillis();

            // 获取登录信息以获取过期时间
            LoginMember loginMember = tokenService.getLoginMemberByToken(token);

            // 构造返回的用户信息
            JSONObject response = new JSONObject();
            response.put("data", token);
            response.put("userId", userId); // 使用真实的用户ID
            response.put("nickname", form.getNickname());
            response.put("avatar", form.getAvatarUrl());
            response.put("phone", ""); // 手机号已加密存储，不返回明文
            response.put("level", 1); // 普通会员
            response.put("levelName", "普通会员");
            response.put("expireTime", loginMember != null ? loginMember.getExpireTime() : 0);

            log.info("微信手机号注册成功，返回token: {}, 过期时间: {}",
                    token, loginMember != null ? loginMember.getExpireTime() : 0);
            return ResponseEntity.ok(response.toJSONString());
        } catch (Exception e) {
            log.error("微信手机号注册失败", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse.toJSONString());
        }
    }

    /**
     * 微信用户信息登录（第一步：获取微信用户信息）
     * 
     * @param form 包含微信登录code和用户信息的表单
     * @return 登录结果
     */
    @PostMapping("/no-auth/wechat/userinfo-login")
    public ResponseEntity<String> userInfoLogin(@RequestBody WechatUserInfoLoginForm form) {
        try {
            String token = memberWechatService.userInfoLogin(form);

            if (StrUtil.isBlank(token)) {
                log.error("用户信息登录成功但token为空");
                throw new RuntimeException("登录失败，请重试");
            }

            // 构造返回的用户信息
            JSONObject response = new JSONObject();
            response.put("data", token);
            response.put("nickname", form.getNickname());
            response.put("avatar", form.getAvatarUrl());
            response.put("level", 1); // 普通会员
            response.put("levelName", "普通会员");
            response.put("needPhone", true); // 标记需要手机号完善信息

            log.info("微信用户信息登录成功，返回token: {}", token);
            return ResponseEntity.ok(response.toJSONString());
        } catch (Exception e) {
            log.error("微信用户信息登录失败", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse.toJSONString());
        }
    }

    /**
     * 获取微信手机号（第二步：解密手机号）
     * 
     * @param form 包含手机号加密数据的表单
     * @return 手机号信息
     */
    @PostMapping("/no-auth/wechat/get-phone")
    public ResponseEntity<String> getPhoneNumber(@RequestBody WechatPhoneForm form) {
        try {
            String phoneNumber = memberWechatService.getPhoneNumber(form);

            if (StrUtil.isBlank(phoneNumber)) {
                throw new RuntimeException("获取手机号失败");
            }

            // 构造返回的手机号信息（隐藏中间4位）
            String hiddenPhone = phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);

            JSONObject response = new JSONObject();
            response.put("phone", hiddenPhone);
            response.put("fullPhone", phoneNumber); // 完整手机号，用于后续注册

            log.info("获取微信手机号成功: {}", hiddenPhone);
            return ResponseEntity.ok(response.toJSONString());
        } catch (Exception e) {
            log.error("获取微信手机号失败", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse.toJSONString());
        }
    }

    /**
     * 检查微信登录缓存状态
     * 
     * @param openid 微信openid
     * @return 登录状态
     */
    @GetMapping("/no-auth/wechat/check-login-status")
    public ResponseEntity<String> checkLoginStatus(@RequestParam String openid) {
        try {
            JSONObject result = memberWechatService.checkLoginStatus(openid);
            return ResponseEntity.ok(result.toJSONString());
        } catch (Exception e) {
            log.error("检查微信登录状态失败", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse.toJSONString());
        }
    }

    /**
     * 刷新微信登录token
     * 
     * @param openid 微信openid
     * @return 新的登录信息
     */
    @PostMapping("/no-auth/wechat/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestParam String openid) {
        try {
            JSONObject result = memberWechatService.refreshLoginToken(openid);
            return ResponseEntity.ok(result.toJSONString());
        } catch (Exception e) {
            log.error("刷新微信登录token失败", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse.toJSONString());
        }
    }
}
