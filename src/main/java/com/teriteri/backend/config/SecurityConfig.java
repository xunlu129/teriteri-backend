package com.teriteri.backend.config;

import com.teriteri.backend.config.filter.JwtAuthenticationTokenFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 密码BCrypt加密
     * @return BCrypt加密后的密码
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户名和密码验证
     * @return Authentication对象
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                // 从Authentication对象中获取用户名和身份凭证信息
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();

                UserDetails loginUser = userDetailsService.loadUserByUsername(username);
                if (Objects.isNull(loginUser) || !passwordEncoder().matches(password, loginUser.getPassword())) {
                    // 密码匹配失败抛出异常
                    throw new BadCredentialsException("访问拒绝：用户名或密码错误！");
                }

//                log.info("访问成功：" + loginUser);
                return new UsernamePasswordAuthenticationToken(loginUser, password, loginUser.getAuthorities());
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return authentication.equals(UsernamePasswordAuthenticationToken.class);
            }
        };
    }

    /**
     * 请求接口过滤器，验证是否开放接口，如果不是开放接口请求头又没带 Authorization 属性会被直接拦截
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 基于 token，不需要 csrf
                .csrf().disable()
                // 基于 token，不需要 session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 下面开始设置权限
                .authorizeRequests(authorize -> authorize
                        // 请求放开接口
                        .antMatchers("/druid/**","/favicon.ico",
                                "/user/account/register",
                                "/user/account/login",
                                "/admin/account/login",
                                "/category/getall",
                                "/video/random/visitor",
                                "/video/cumulative/visitor",
                                "/video/getone",
                                "/ws/danmu/**",
                                "/danmu-list/**",
                                "/msg/chat/outline",
                                "/video/play/visitor",
                                "/favorite/get-all/visitor",
                                "/search/**",
                                "/comment/get",
                                "/comment/reply/get-more",
                                "/comment/get-up-like",
                                "/user/info/get-one",
                                "/video/user-works-count",
                                "/video/user-works",
                                "/video/user-love",
                                "/video/user-collect").permitAll()
                        // 允许HTTP OPTIONS请求
                        .antMatchers(HttpMethod.OPTIONS).permitAll()
                        // 其他地址的访问均需验证权限
                        .anyRequest().authenticated()
                )
                // 添加 JWT 过滤器，JWT 过滤器在用户名密码认证过滤器之前
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
