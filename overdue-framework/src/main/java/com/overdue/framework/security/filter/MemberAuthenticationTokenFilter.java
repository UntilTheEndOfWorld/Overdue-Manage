package com.overdue.framework.security.filter;

import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.model.LoginMember;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 会员token过滤器 验证会员token有效性并实现滑动过期
 *
 * @author ruoyi
 */
@Component
public class MemberAuthenticationTokenFilter extends OncePerRequestFilter {
  private Lock lock = new ReentrantLock();
  @Autowired
  private TokenService tokenService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    // 检查是否是H5接口请求
    String requestURI = request.getRequestURI();
    if (requestURI.startsWith("/h5/") && !requestURI.contains("/no-auth/")) {
      // 获取会员token
      LoginMember loginMember = tokenService.getLoginMember(request);
      if (StringUtils.isNotNull(loginMember)
          && StringUtils.isNull(SecurityContextHolder.getContext().getAuthentication())) {
        // 验证并刷新会员token（滑动过期）
        tokenService.verifyMemberToken(loginMember);

        // 创建认证上下文（会员用户没有复杂的权限体系，传递null作为权限）
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginMember, null, null);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 在响应头中返回新的过期时间（可选）
        response.addHeader("X-Token-Expire-Time", String.valueOf(loginMember.getExpireTime()));
      }
    }

    // 添加请求追踪ID
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
}
