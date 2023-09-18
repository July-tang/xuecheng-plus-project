package com.xuecheng.messagesdk.config;

import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author july
 */
@Slf4j
@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Resource
    MqMessageService mqMessageService;

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
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        // 设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);

        // 确认消息送到交换机(Exchange)回调
        // 如果消息没有到 exchange,则 confirm 回调,ack=false; 如果消息到达exchange,则confirm回调,ack=true
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (Objects.isNull(correlationData)) {
                return;
            }
            if (!ack) {
                int tryNum = 3;
                for (int i = 0; i < tryNum; i++) {
                    try {
                        if (mqMessageService.failMessage(correlationData.getId())) {
                            return;
                        }
                    } catch (Exception e) {
                        log.error("消息标记失败：{}", correlationData.getId(), e);
                    }
                }
            }
        });
        return rabbitTemplate;
    }

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
