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

    public static final String VIDEO_PROCESS_EXCHANGE = "video_process_exchange";

    public static final String VIDEO_PROCESS_QUEUE = "video_process_queue";

    public static final String VIDEO_PROCESS = "video_process";

    public static final String COURSE_PUBLISH_EXCHANGE = "course_publish_exchange";

    public static final String COURSE_STATICS_QUEUE = "course_statics_queue";

    public static final String COURSE_PUBLISH = "course_publish";

    public static final String ADD_INDEX_QUEUE = "add_index_queue";

    public static final String PAY_NOTIFY_EXCHANGE = "pay_notify_exchange";

    public static final String PAY_NOTIFY_QUEUE = "pay_notify_queue";

    public static final String PAY_NOTIFY = "pay_notify";

    @Bean
    public FanoutExchange payNotifyExchange(){
        return new FanoutExchange(PAY_NOTIFY_EXCHANGE, true, false);
    }

    @Bean
    public Queue payNotifyQueue(){
        return QueueBuilder.durable(PAY_NOTIFY_QUEUE).build();
    }

    @Bean
    public Binding payNotifyBinding(Queue payNotifyQueue,
                                    FanoutExchange payNotifyExchange) {
        return BindingBuilder.bind(payNotifyQueue).to(payNotifyExchange);
    }

    @Bean
    public Queue videoProcessQueue() {
        return QueueBuilder.durable(VIDEO_PROCESS_QUEUE).build();
    }

    @Bean
    public DirectExchange videoProcessExchange() {
        return ExchangeBuilder.directExchange(VIDEO_PROCESS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding videoProcessBinding(Queue videoProcessQueue,
                                       DirectExchange videoProcessExchange) {
        return BindingBuilder.bind(videoProcessQueue).to(videoProcessExchange).with(VIDEO_PROCESS);
    }

    @Bean
    public Queue courseStaticsQueue() {
        return QueueBuilder.durable(COURSE_STATICS_QUEUE).build();
    }

    @Bean
    public FanoutExchange coursePublishExchange() {
        return ExchangeBuilder.fanoutExchange(COURSE_PUBLISH_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding courseStaticsBinding(Queue courseStaticsQueue,
                                        FanoutExchange coursePublishExchange) {
        return BindingBuilder.bind(courseStaticsQueue).to(coursePublishExchange);
    }

    @Bean
    public Queue addIndexQueue() {
        return QueueBuilder.durable(ADD_INDEX_QUEUE).build();
    }

    @Bean
    public Binding addIndexBinding(Queue addIndexQueue,
                                   FanoutExchange coursePublishExchange) {
        return BindingBuilder.bind(addIndexQueue).to(coursePublishExchange);
    }
}
