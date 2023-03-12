package com.xuecheng.checkcode.service.impl;

import com.xuecheng.checkcode.service.CheckCodeService;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 数字验证码生成器
 *
 * @author july
 */
@Component("NumberCheckCodeGenerator")
public class NumberCheckCodeGenerator implements CheckCodeService.CheckCodeGenerator{
    @Override
    public String generate(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(10);
            sb.append(number);
        }
        return sb.toString();
    }
}
