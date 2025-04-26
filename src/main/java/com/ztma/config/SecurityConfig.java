package com.ztma.config;

import com.ztma.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .authorizeHttpRequests()
	            .requestMatchers(
	                "/register", "/verify", "/verify-email", "/login",
	                "/css/**", "/js/**", "/webjars/**", "/chat-websocket/**"
	            ).permitAll()
	            .anyRequest().authenticated()
	            .and()
	        .formLogin()
	            .loginPage("/login")
	            .defaultSuccessUrl("/chat", true)
	            .permitAll()
	            .and()
	        .logout()
	            .logoutSuccessUrl("/login")
	            .permitAll()
	            .and()
	        .csrf().disable(); // For now, disable CSRF because WebSocket + form-login causes issues otherwise

	    return http.build();
	}



	@Bean
	public DaoAuthenticationProvider authProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(new BCryptPasswordEncoder());
		return provider;
	}
}
