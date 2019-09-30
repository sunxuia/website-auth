package net.sunxu.website.auth.service.config;

import net.sunxu.website.auth.service.service.UserInfoService;
import net.sunxu.website.config.security.authentication.WebsiteSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebsiteSecurityConfig {

    private static final String LOGIN_URL = "/login";

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    @Qualifier("socialFilter")
    @Lazy
    private CompositeFilter socialFilter;

    @Autowired
    private LogInSuccessfulHandler logInSuccessfulHandler;

    @Autowired
    private NotifiableLogoutHandler notifiableLogoutHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userInfoService).passwordEncoder(passwordEncoder());
        auth.eraseCredentials(false);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.formLogin()
                .loginPage(LOGIN_URL).loginProcessingUrl(LOGIN_URL).usernameParameter("user-name")
                .passwordParameter("password")
                .successHandler(logInSuccessfulHandler)
                .failureForwardUrl("/login?error=登录失败")
                .and().authorizeRequests()
                .antMatchers("/",
                        "/info",
                        "/register",
                        LOGIN_URL,
                        "/login/**",
                        "/error",
                        "/favicon.ico",
                        "/js/**",
                        "/css/**",
                        "/font/**",
                        "/image/**",
                        "/auth/**"
                ).permitAll()
                .anyRequest().authenticated().and()
                .addFilterBefore(jwtAuthenticationTokenFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(socialFilter, BasicAuthenticationFilter.class)
                .exceptionHandling().accessDeniedPage("/deny")
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and().logout().addLogoutHandler(notifiableLogoutHandler).logoutSuccessUrl(LOGIN_URL)
                .and().csrf().ignoringAntMatchers("/auth/**")
                .and().rememberMe().disable();

    }

}
