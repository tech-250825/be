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

    public static final String FACE_DETAILER_EXCHANGE = "facedetailer.exchange";
    public static final String FACE_DETAILER_QUEUE = "facedetailer.queue";
    public static final String FACE_DETAILER_CREATE_ROUTING_KEY = "facedetailer.create";

    public static final String PLAIN_IMAGE_EXCHANGE = "plain.exchange";
    public static final String PLAIN_IMAGE_QUEUE = "plain.queue";
    public static final String PLAIN_IMAGE_CREATE_ROUTING_KEY = "plain.create";

    public static final String T2V_EXCHANGE = "t2v.exchange";
    public static final String T2V_QUEUE = "t2v.queue";
    public static final String T2V_ROUTING_KEY = "t2v.create";

    public static final String I2V_EXCHANGE = "i2v.exchange";
    public static final String I2V_QUEUE = "i2v.queue";
    public static final String I2V_ROUTING_KEY = "i2v.create";

    public static final String I2I_EXCHANGE = "i2i.exchange";
    public static final String I2I_QUEUE = "i2i.queue";
    public static final String I2I_ROUTING_KEY = "i2i.create";

    public static final String DOWNLOAD_EXCHANGE = "download.exchange";
    public static final String DOWNLOAD_QUEUE = "download.queue";
    public static final String DOWNLOAD_ROUTING_KEY = "download.create";

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
    public Queue faceDetailerQueue() {
        return new Queue(FACE_DETAILER_QUEUE, true);
    }

    @Bean
    public TopicExchange faceDetailerExchange() {
        return new TopicExchange(FACE_DETAILER_EXCHANGE);
    }

    @Bean
    public Binding faceDetailerBinding(@Qualifier("faceDetailerQueue") Queue queue,
                                @Qualifier("faceDetailerExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(FACE_DETAILER_CREATE_ROUTING_KEY);
    }

    @Bean
    public Queue plainQueue() {
        return new Queue(PLAIN_IMAGE_QUEUE, true);
    }

    @Bean
    public TopicExchange plainExchange() {
        return new TopicExchange(PLAIN_IMAGE_EXCHANGE);
    }

    @Bean
    public Binding plainBinding(@Qualifier("plainQueue") Queue queue,
                                       @Qualifier("plainExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(PLAIN_IMAGE_CREATE_ROUTING_KEY);
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
    public Queue i2iQueue() {
        return new Queue(I2I_QUEUE, true);
    }

    @Bean
    public TopicExchange i2iExchange() {
        return new TopicExchange(I2I_EXCHANGE);
    }

    @Bean
    public Binding i2iBinding(@Qualifier("i2iQueue") Queue queue,
                              @Qualifier("i2iExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(I2I_ROUTING_KEY);
    }

    @Bean
    public Queue downloadQueue() {
        return new Queue(DOWNLOAD_QUEUE, true);
    }

    @Bean
    public TopicExchange downloadExchange() {
        return new TopicExchange(DOWNLOAD_EXCHANGE);
    }

    @Bean
    public Binding downloadBinding(@Qualifier("downloadQueue") Queue queue,
                              @Qualifier("downloadExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(DOWNLOAD_ROUTING_KEY);
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