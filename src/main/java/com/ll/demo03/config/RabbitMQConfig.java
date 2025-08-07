package com.ll.demo03.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String IMAGE_EXCHANGE = "image.exchange";
    public static final String IMAGE_QUEUE = "image.queue";
    public static final String IMAGE_CREATE_ROUTING_KEY = "image.create";

    public static final String T2V_EXCHANGE = "t2v.exchange";
    public static final String T2V_QUEUE = "t2v.queue";
    public static final String T2V_ROUTING_KEY = "t2v.create";

    public static final String I2V_EXCHANGE = "i2v.exchange";
    public static final String I2V_QUEUE = "i2v.queue";
    public static final String I2V_ROUTING_KEY = "i2v.create";

    @Bean
    public Queue imageQueue() {
        return new Queue(IMAGE_QUEUE, true);
    }

    @Bean
    public TopicExchange imageExchange() {
        return new TopicExchange(IMAGE_EXCHANGE);
    }

    @Bean
    public Binding imageBinding(@Qualifier("imageQueue") Queue queue,
                                @Qualifier("imageExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(IMAGE_CREATE_ROUTING_KEY);
    }


    @Bean
    public Queue t2vQueue() {
        return new Queue(T2V_QUEUE, true);
    }

    @Bean
    public TopicExchange t2vExchange() {
        return new TopicExchange(T2V_EXCHANGE);
    }

    @Bean
    public Binding t2vBinding(@Qualifier("t2vQueue") Queue queue,
                                @Qualifier("t2vExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(T2V_ROUTING_KEY);
    }

    @Bean
    public Queue i2vQueue() {
        return new Queue(I2V_QUEUE, true);
    }

    @Bean
    public TopicExchange i2vExchange() {
        return new TopicExchange(I2V_EXCHANGE);
    }

    @Bean
    public Binding i2vBinding(@Qualifier("i2vQueue") Queue queue,
                              @Qualifier("i2vExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(I2V_ROUTING_KEY);
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
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        return factory;
    }
}