package com.msa7.v1.order.global.config;

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
public class RabbitConfig {
	public static final String DELIVERY_EXCHANGE = "delivery-exchange";
	public static final String DELIVERY_CREATED_QUEUE = "delivery-created-queue";
	public static final String DELIVERY_CREATED_ROUTING_KEY = "delivery.created";
	public static final String DELIVERY_FAIL_QUEUE= "delivery-fail-queue";
	public static final String DELIVERY_FAIL_ROUTING_KEY = "delivery.fail";

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public DirectExchange deliveryExchange() {
		return new DirectExchange(DELIVERY_EXCHANGE);
	}
	@Bean
	public Queue deliverySuccessQueue(){
		return QueueBuilder.durable(DELIVERY_CREATED_QUEUE).build();
	}

	@Bean
	public Queue deliveryFailQueue() {
		return QueueBuilder.durable(DELIVERY_FAIL_QUEUE).build();
	}

	@Bean
	public Binding deliveryFailBinding(Queue deliveryFailQueue, DirectExchange directExchange){
		return BindingBuilder.bind(deliveryFailQueue).to(directExchange).with(DELIVERY_FAIL_ROUTING_KEY);}

	@Bean
	public Binding deliverySuccessBinding(Queue deliverySuccessQueue, DirectExchange directExchange){
		return BindingBuilder.bind(deliverySuccessQueue).to(directExchange).with(DELIVERY_CREATED_ROUTING_KEY);
	}

}