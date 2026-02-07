package com.overdue.web.filter;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.model.LoginMember;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.manager.item.domain.entity.OverdueUser;
import com.overdue.manager.item.service.OverdueUserService;
import com.overdue.manager.ums.domain.entity.MemberWechat;
import com.overdue.manager.ums.mapper.MemberWechatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 过期了吗小程序接口鉴权：从会员 token 解析出 Overdue 用户 ID 并放入 ThreadLocal，供
 * /personal、/shared、/item/calendar 使用。
 *
 * @author overdue
 */
@Component
public class OverdueMiniappAuthFilter extends OncePerRequestFilter {

    @Autowired
    private MemberWechatMapper memberWechatMapper;
    @Autowired
    private OverdueUserService overdueUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        // 跳过非目标路径
        if (!uri.startsWith("/personal/") && !uri.startsWith("/shared/") && !uri.startsWith("/item/calendar/")
                && !uri.startsWith("/member/")) {
            chain.doFilter(request, response);
            return;
        }
        // 跳过公开接口（无需登录）
        if (uri.contains("/no-auth/")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null
                    && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof LoginMember) {
                LoginMember loginMember = (LoginMember) SecurityContextHolder.getContext().getAuthentication()
                        .getPrincipal();
                Long memberId = loginMember.getMemberId();
                if (memberId != null) {
                    MemberWechat wechat = memberWechatMapper.selectByMemberId(memberId);
                    if (wechat != null && wechat.getOpenid() != null) {
                        OverdueUser overdueUser = overdueUserService.selectByOpenid(wechat.getOpenid());
                        if (overdueUser != null && overdueUser.getId() != null) {
                            LocalDataUtil.setVar(Constants.OVERDUE_USER_ID, overdueUser.getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 不阻断请求，控制器内可再判空
        }
        chain.doFilter(request, response);
    }
}
