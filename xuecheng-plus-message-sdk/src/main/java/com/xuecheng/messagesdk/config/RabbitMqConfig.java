package com.xuecheng.messagesdk.config;

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

    public static final String VIDEO_PROCESS = "video_process";

    public static final String COURSE_PUBLISH_EXCHANGE_NAME = "course_publish_exchange";

    public static final String COURSE_STATICS_QUEUE_NAME = "course_statics_queue";

    public static final String COURSE_STATICS = "course_statics";

    public static final String ADD_INDEX_QUEUE_NAME = "add_index_queue";

    public static final String ADD_INDEX = "add_index";

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
        return BindingBuilder.bind(videoProcessQueue).to(videoProcessExchange).with(VIDEO_PROCESS);
    }

    @Bean
    public Queue courseStaticsQueue() {
        return QueueBuilder.durable(COURSE_STATICS_QUEUE_NAME).build();
    }

    @Bean
    public DirectExchange coursePublishExchange() {
        return ExchangeBuilder.directExchange(COURSE_PUBLISH_EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    public Binding courseStaticsBinding(Queue courseStaticsQueue,
                                       DirectExchange coursePublishExchange) {
        return BindingBuilder.bind(courseStaticsQueue).to(coursePublishExchange).with(COURSE_STATICS);
    }

    @Bean
    public Queue addIndexQueue() {
        return QueueBuilder.durable(ADD_INDEX_QUEUE_NAME).build();
    }

    @Bean
    public Binding addIndexBinding(Queue addIndexQueue,
                                        DirectExchange coursePublishExchange) {
        return BindingBuilder.bind(addIndexQueue).to(coursePublishExchange).with(ADD_INDEX);
    }
}
