package com.xuecheng.base.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author july
 */
@Configuration
@EnableRabbit
public class RabbitMqConfig {

    public static final String VIDEO_PROCESS_EXCHANGE_NAME = "video_process_exchange";

    public static final String VIDEO_PROCESS_QUEUE_NAME = "video_process_queue";

    public static final String VIDEO_PROCESS_ROUTING_KEY = "video_process_routingKey";

    @Bean
    public Queue videoProcessQueue() {
        return QueueBuilder.durable(VIDEO_PROCESS_QUEUE_NAME).build();
    }

    @Bean
    public DirectExchange videoProcessExchange() {
        return ExchangeBuilder.directExchange(VIDEO_PROCESS_EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    public Binding videoProcessBinding(Queue videoProcessQueue,
                                       DirectExchange videoProcessExchange) {
        return BindingBuilder.bind(videoProcessQueue).to(videoProcessExchange).with(VIDEO_PROCESS_ROUTING_KEY);
    }
}
