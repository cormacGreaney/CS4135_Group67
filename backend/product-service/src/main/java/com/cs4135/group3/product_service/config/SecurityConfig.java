package com.cs4135.group3.product_service.config;

import com.cs4135.group3.product_service.exception.ApiError;
import com.cs4135.group3.product_service.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Who can call what:
// - Anyone can browse products (GET).
// - Only admins can add, edit, or delete. That comes from the role on the JWT after you log in via user-service.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			ObjectMapper objectMapper) throws Exception {
		http
				// REST API — no browser form posts, so CSRF off. Sessions off; we use JWT on each request.
				.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
						.accessDeniedHandler(accessDeniedHandler(objectMapper)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/error").permitAll()
						.requestMatchers("/internal/**").permitAll()
						// No login needed to list or view products
						.requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
						// Add, change, or remove products — logged-in admin only
						.requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMINISTRATOR")
						.requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMINISTRATOR")
						.requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMINISTRATOR")
						.anyRequest().denyAll())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	// When someone is logged in but not an admin, return JSON 403 instead of an HTML error page
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
