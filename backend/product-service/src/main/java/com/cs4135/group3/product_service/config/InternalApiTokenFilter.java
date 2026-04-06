package com.cs4135.group3.product_service.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(0)
public class InternalApiTokenFilter extends OncePerRequestFilter {

	@Value("${app.internal-api.token}")
	private String expectedToken;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		if (!request.getRequestURI().startsWith("/internal/")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = request.getHeader("X-Internal-Token");
		if (expectedToken == null || !expectedToken.equals(token)) {
			response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing internal token");
			return;
		}

		filterChain.doFilter(request, response);
	}
}
