package com.overdue.web.core.config;

import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.service.MemberService;
import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.model.LoginMember;
import com.overdue.common.exception.ServiceException;
import com.overdue.framework.config.LocalDataUtil;
import com.overdue.framework.web.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class H5MemberInterceptor extends HandlerInterceptorAdapter {

  @Autowired
  private TokenService tokenService;
  @Autowired
  private MemberService memberService;

  private static String[] WHITE_PATHS = {
      "/h5/sms/login",
      "/h5/wechat/login",
      "/h5/account/login",
      "/h5/register",
      "/h5/validate"
  };

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String requestUri = request.getRequestURI();

    // 正常模式的拦截器逻辑
    boolean flag = true;
    if (!requestUri.startsWith("/h5/")) {
      return super.preHandle(request, response, handler);
    }

    for (String s : WHITE_PATHS) {
      if (requestUri.startsWith(s)) {
        flag = false;
        break;
      }
    }
    if (!flag) {
      return super.preHandle(request, response, handler);
    }
    LoginMember loginMember = tokenService.getLoginMember(request);

    if (loginMember == null) {
      throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED.value());
    }

    tokenService.verifyMemberToken(loginMember);
    // 获取会员信息
    Member member = memberService.selectById(loginMember.getMemberId());

    if (member == null || member.getStatus() == 0) {
      throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED.value());
    }

    // 将会员信息存放至全局
    LocalDataUtil.setVar(Constants.MEMBER_INFO, member);

    return super.preHandle(request, response, handler);
  }
}
