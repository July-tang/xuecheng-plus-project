package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author July
 */
@EnableFeignClients(basePackages = {"com.xuecheng.content.feign"})
@EnableSwagger2Doc
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = "com.xuecheng")
public class ContentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApiApplication.class, args);
    }

}
