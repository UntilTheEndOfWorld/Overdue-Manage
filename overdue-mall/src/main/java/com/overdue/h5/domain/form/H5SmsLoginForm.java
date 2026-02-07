package com.overdue.h5.domain.form;

import com.overdue.wechat.response.WechatUserAuth;
import lombok.Data;

@Data
public class H5SmsLoginForm extends H5LoginForm {
    /** 验证码 */
    private String code;
    /** uuid */
    private String uuid;
    /** 微信授权信息 */
    private WechatUserAuth authInfo;
    /** 小程序openid **/
    private String mpOpenId;
}
