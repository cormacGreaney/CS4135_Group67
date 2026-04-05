package com.cs4135.group3.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cs4135.group3.order_service.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/error").permitAll()
						// Internal callbacks keep using X-Internal-Token via InternalApiTokenFilter.
						.requestMatchers("/internal/**").permitAll()
						.requestMatchers("/api/order/**").authenticated()
						.anyRequest().denyAll())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
