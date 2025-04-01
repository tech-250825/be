package com.ll.demo03.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String IMAGE_EXCHANGE = "image.exchange";
    public static final String IMAGE_QUEUE = "image.queue";
    public static final String IMAGE_CREATE_ROUTING_KEY = "image.create";


    public static final String UPSCALE_EXCHANGE = "upscale.exchange";
    public static final String UPSCALE_QUEUE = "upscale.queue";
    public static final String UPSCALE_ROUTING_KEY = "upscale.create";


    public static final String VIDEO_EXCHANGE = "video.exchange";
    public static final String VIDEO_QUEUE = "video.queue";
    public static final String VIDEO_ROUTING_KEY = "video.create";

    @Bean
    public Queue imageQueue() {
        return new Queue(IMAGE_QUEUE, true);
    }

    @Bean
    public TopicExchange imageExchange() {
        return new TopicExchange(IMAGE_EXCHANGE);
    }

    @Bean
    public Binding imageBinding(Queue imageQueue, TopicExchange imageExchange) {
        return BindingBuilder.bind(imageQueue).to(imageExchange).with(IMAGE_CREATE_ROUTING_KEY);
    }

    // Upscale Queue and Binding
    @Bean
    public Queue upscaleQueue() {
        return new Queue(UPSCALE_QUEUE, true);
    }

    @Bean
    public TopicExchange upscaleExchange() {
        return new TopicExchange(UPSCALE_EXCHANGE);
    }

    @Bean
    public Binding upscaleBinding(Queue upscaleQueue, TopicExchange upscaleExchange) {
        return BindingBuilder.bind(upscaleQueue).to(upscaleExchange).with(UPSCALE_ROUTING_KEY);
    }

    @Bean
    public Queue videoQueue() {
        return new Queue(VIDEO_QUEUE, true);
    }

    @Bean
    public TopicExchange videoExchange() {
        return new TopicExchange(VIDEO_EXCHANGE);
    }

    @Bean
    public Binding videoBinding(Queue videoQueue, TopicExchange videoExchange) {
        return BindingBuilder.bind(videoQueue).to(videoExchange).with(VIDEO_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); 
        return factory;
    }
}