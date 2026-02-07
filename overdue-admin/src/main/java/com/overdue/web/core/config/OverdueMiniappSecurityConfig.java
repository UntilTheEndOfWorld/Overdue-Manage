package com.overdue.web.core.config;

import com.overdue.framework.security.filter.MemberAuthenticationTokenFilter;
import com.overdue.web.filter.OverdueMiniappAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 过期了吗小程序鉴权：在会员 token 校验之后设置 Overdue 用户 ID 到 ThreadLocal。
 *
 * @author overdue
 */
@Order(100)
public class OverdueMiniappSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private OverdueMiniappAuthFilter overdueMiniappAuthFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(overdueMiniappAuthFilter, MemberAuthenticationTokenFilter.class);
    }
}
