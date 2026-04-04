package com.cs4135.group3.payment_service.messaging;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	private static final String ORDER_CREATED_EVENT_FQCN = "com.cs4135.group3.order_service.events.OrderCreatedEvent";

	@Bean
	TopicExchange ecommerceTopicExchange() {
		return new TopicExchange(MessagingConstants.ECOMMERCE_TOPIC_EXCHANGE, true, false);
	}

	@Bean
	Queue paymentOrderCreatedQueue() {
		return new Queue(MessagingConstants.PAYMENT_ORDER_CREATED_QUEUE, true);
	}

	@Bean
	Binding paymentOrderCreatedBinding(TopicExchange ecommerceTopicExchange, Queue paymentOrderCreatedQueue) {
		return BindingBuilder.bind(paymentOrderCreatedQueue)
				.to(ecommerceTopicExchange)
				.with(MessagingConstants.ORDER_CREATED_ROUTING_KEY);
	}

	@Bean
	MessageConverter jsonMessageConverter() {
		Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
		DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
		Map<String, Class<?>> mappings = new HashMap<>();
		mappings.put(ORDER_CREATED_EVENT_FQCN, OrderCreatedMessage.class);
		typeMapper.setIdClassMapping(mappings);
		converter.setJavaTypeMapper(typeMapper);
		return converter;
	}

	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter);
		return template;
	}

	@Bean
	SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory,
			MessageConverter jsonMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(jsonMessageConverter);
		return factory;
	}
}
