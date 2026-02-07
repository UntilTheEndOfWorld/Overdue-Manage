package com.overdue.h5.domain.form;

import lombok.Data;

/**
 * 微信手机号注册表单
 */
@Data
public class WechatPhoneRegisterForm {
  /**
   * 微信登录code（用于新用户注册）
   */
  private String code;

  /**
   * 临时token（用于已登录用户绑定手机号）
   */
  private String token;

  /**
   * 手机号加密数据
   */
  private String encryptedData;

  /**
   * 加密算法的初始向量
   */
  private String iv;

  /**
   * 用户昵称（可选）
   */
  private String nickname;

  /**
   * 用户头像（可选）
   */
  private String avatarUrl;
}
