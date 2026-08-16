package com.msa7.v1.delivery.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {
	public static final String ORDER_EXCHANGE = "order-exchange";
	public static final String ORDER_CREATED_QUEUE = "order-created-queue";
	public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public DirectExchange orderExchange() {
		return new DirectExchange(ORDER_EXCHANGE);
	}
	@Bean
	public Queue orderCreatedQueue(){
		return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
	}

	@Bean
	public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange orderExchange){
		return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(ORDER_CREATED_ROUTING_KEY);
	}

}
