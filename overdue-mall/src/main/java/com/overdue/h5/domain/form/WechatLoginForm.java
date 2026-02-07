package com.overdue.h5.domain.form;

import lombok.Data;

@Data
public class WechatLoginForm {
    private String code;
    private String state;
    private String nickname;
    private String avatarUrl;
}
