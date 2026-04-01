package com.cs4135.group3.payment_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cs4135.group3.payment_service.exception.ApiError;
import com.cs4135.group3.payment_service.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			ObjectMapper objectMapper) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
						.accessDeniedHandler(accessDeniedHandler(objectMapper)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/error").permitAll()
						.requestMatchers("/api/payments/**").authenticated()
						.anyRequest().denyAll())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private static AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
		return (request, response, accessDeniedException) -> {
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			ApiError body = ApiError.of(
					HttpStatus.FORBIDDEN.value(),
					"Forbidden",
					"Access denied",
					request.getRequestURI());
			objectMapper.writeValue(response.getOutputStream(), body);
		};
	}
}
