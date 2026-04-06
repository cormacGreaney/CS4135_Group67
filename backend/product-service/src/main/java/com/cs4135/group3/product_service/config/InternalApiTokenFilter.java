package com.cs4135.group3.product_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(0)
// Guards /internal/* endpoints used by other backend services with a shared token header.
public class InternalApiTokenFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Internal-Token";

	@Value("${app.internal-api.token}")
	private String expectedToken;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String path = request.getRequestURI();
		if (!path.startsWith("/internal/")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = request.getHeader(HEADER);
		if (expectedToken == null || !expectedToken.equals(token)) {
			response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing internal token");
			return;
		}

		filterChain.doFilter(request, response);
	}
}
