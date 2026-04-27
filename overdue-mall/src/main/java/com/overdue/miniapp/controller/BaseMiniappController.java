package com.overdue.miniapp.controller;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
import org.springframework.http.HttpStatus;

import java.util.function.Function;

/**
 * 小程序控制器公共基类，统一登录态与当前用户信息获取。
 */
public abstract class BaseMiniappController {
    protected static final String MSG_LOGIN_REQUIRED = "请先登录";

    protected Long getCurrentUserId() {
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

    protected String getCurrentUserName() {
        Object nick = LocalDataUtil.getVar("nickName");
        if (nick == null || String.valueOf(nick).trim().isEmpty()) {
            nick = LocalDataUtil.getVar("nickname");
        }
        return nick == null || String.valueOf(nick).trim().isEmpty() ? "我" : String.valueOf(nick);
    }

    protected AjaxResult withLogin(Function<Long, AjaxResult> action) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(HttpStatus.UNAUTHORIZED.value(), MSG_LOGIN_REQUIRED);
        }
        return action.apply(userId);
    }
}
