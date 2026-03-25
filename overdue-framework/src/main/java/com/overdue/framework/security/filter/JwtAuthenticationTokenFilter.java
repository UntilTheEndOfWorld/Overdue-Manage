package com.overdue.framework.security.filter;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.entity.SysUser;
import com.overdue.common.core.domain.model.LoginMember;
import com.overdue.common.core.domain.model.LoginUser;
import com.overdue.common.utils.SecurityUtils;
import com.overdue.common.utils.StringUtils;
import com.overdue.framework.web.service.TokenService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * token过滤器 验证token有效性
 *
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private Lock lock = new ReentrantLock();
    @Autowired
    private TokenService tokenService;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // HTTP请求安全检查 - 防止非法请求
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String userAgent = request.getHeader("User-Agent");

            // 检查HTTP方法是否合法
            if (method == null || method.trim().isEmpty() ||
                    !Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH")
                            .contains(method.toUpperCase())) {
                log.warn("非法HTTP方法: {}, URI: {}", method, uri);
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return;
            }

            // 检查URI是否包含非法字符
            if (uri != null && (uri.contains("\0") || uri.contains("\r") || uri.contains("\n"))) {
                log.warn("URI包含非法字符: {}", uri);
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return;
            }

            // 检查User-Agent是否包含非法字符
            if (userAgent != null && (userAgent.contains("\0") || userAgent.length() > 1000)) {
                log.warn("User-Agent包含非法字符或过长: {}", userAgent.substring(0, Math.min(100, userAgent.length())));
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return;
            }

        } catch (Exception e) {
            log.error("HTTP请求安全检查异常", e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }

        // 正常JWT验证逻辑
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication())) {
            tokenService.verifyToken(loginUser);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 会员token验证和刷新（滑动过期），并写入 SecurityContext，供 OverdueMiniappAuthFilter 等使用
        LoginMember loginMember = tokenService.getLoginMember(request);
        if (StringUtils.isNotNull(loginMember)) {
            tokenService.verifyMemberToken(loginMember);
            if (StringUtils.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                UsernamePasswordAuthenticationToken memberAuth = new UsernamePasswordAuthenticationToken(
                        loginMember, null, null);
                memberAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(memberAuth);
            }
        }

        boolean locked = false;
        try {
            locked = lock.tryLock(2, TimeUnit.MILLISECONDS);
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            MDC.put(Constants.SPAN_ID, uuid);
            response.addHeader(Constants.SPAN_ID, uuid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * 创建默认登录用户（开发模式专用）
     */
    private LoginUser createDefaultLoginUser() {
        SysUser defaultUser = new SysUser();
        defaultUser.setUserId(1L);
        defaultUser.setUserName("testuser");
        defaultUser.setNickName("测试用户");
        defaultUser.setPhonenumber("13800138000");
        defaultUser.setEmail("test@example.com");
        defaultUser.setStatus("0"); // 正常状态

        Set<String> permissions = new HashSet<>();
        permissions.add("*:*:*"); // 开发模式给予所有权限

        LoginUser loginUser = new LoginUser(1L, 1L, defaultUser, permissions);
        loginUser.setToken("development_token_default");
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(System.currentTimeMillis() + 30 * 60 * 1000); // 30分钟过期
        loginUser.setIpaddr("127.0.0.1");
        loginUser.setLoginLocation("开发环境");
        loginUser.setBrowser("Development");
        loginUser.setOs("Development");

        return loginUser;
    }
}
