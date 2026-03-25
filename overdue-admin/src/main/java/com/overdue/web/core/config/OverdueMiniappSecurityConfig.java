package com.overdue.web.core.config;

import com.overdue.framework.security.filter.JwtAuthenticationTokenFilter;
import com.overdue.web.filter.OverdueMiniappAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 过期了吗小程序鉴权：必须在 JwtAuthenticationTokenFilter 解析会员 JWT 并写入 LoginMember 之后再执行，
 * 否则无法从 SecurityContext 取得 LoginMember，也无法设置 Overdue 用户 ID。
 *
 * @author overdue
 */
@Order(100)
public class OverdueMiniappSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private OverdueMiniappAuthFilter overdueMiniappAuthFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(overdueMiniappAuthFilter, JwtAuthenticationTokenFilter.class);
    }
}
