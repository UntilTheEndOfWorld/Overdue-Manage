package com.overdue.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.overdue.common.constant.HttpStatus;
import com.overdue.common.core.domain.model.LoginMember;
import com.overdue.common.exception.ServiceException;

/**
 * 会员安全服务工具类
 * 
 * @author ruoyi
 */
public class MemberSecurityUtils {

  /**
   * 获取会员ID
   **/
  public static Long getMemberId() {
    try {
      return getLoginMember().getMemberId();
    } catch (Exception e) {
      throw new ServiceException("获取会员ID异常", HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * 获取会员信息
   **/
  public static LoginMember getLoginMember() {
    try {
      return (LoginMember) getAuthentication().getPrincipal();
    } catch (Exception e) {
      throw new ServiceException("获取会员信息异常", HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * 获取Authentication
   */
  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /**
   * 检查是否已认证
   */
  public static boolean isAuthenticated() {
    try {
      Authentication authentication = getAuthentication();
      return authentication != null && authentication.isAuthenticated();
    } catch (Exception e) {
      return false;
    }
  }
}
