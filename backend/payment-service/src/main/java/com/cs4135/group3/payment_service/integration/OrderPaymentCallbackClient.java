package com.cs4135.group3.payment_service.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.cs4135.group3.payment_service.messaging.PaymentCompletedMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentCallbackClient {

	private final RestTemplate restTemplate;

	@Value("${app.order-service.base-url:http://localhost:8085}")
	private String orderServiceBaseUrl;

	@Value("${app.internal-api.token}")
	private String internalToken;

	public void notifyOrderService(PaymentCompletedMessage message) {
		String url = orderServiceBaseUrl.replaceAll("/$", "")
				+ "/internal/orders/" + message.orderId() + "/payment-result";
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Internal-Token", internalToken);
			restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(message, headers), Void.class);
			log.debug("Internal callback OK for orderId={}", message.orderId());
		}
		catch (RestClientException ex) {
			log.error("Internal callback failed for orderId={}", message.orderId(), ex);
		}
	}
}
