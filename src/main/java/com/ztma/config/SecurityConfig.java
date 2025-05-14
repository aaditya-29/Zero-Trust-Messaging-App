package com.ztma.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ztma.service.CustomUserDetailsService;
import com.ztma.util.JwtUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/register", "/verify", "/verify-email", "/login", "/logout",
                                 "/css/**", "/js/**", "/webjars/**", "/chat-websocket/**",
                                 "/process-login", "/verify-otp", "/process-login-otp")
                .permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")) // ⬅️ Redirect if unauthorized
            .and()
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?expired=true") // ⬅️ Redirect on session invalid
            )
            .rememberMe()
                .key("ZTMA_SecureKey")
                .tokenValiditySeconds(900); // 15 minutes

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().invalidSessionUrl("/login?expired=true").maximumSessions(1)
                .expiredUrl("/login?session=expired");

        http.rememberMe().key("ZTMA_SecureKey").tokenValiditySeconds(900); // 15 min session
    }

}
