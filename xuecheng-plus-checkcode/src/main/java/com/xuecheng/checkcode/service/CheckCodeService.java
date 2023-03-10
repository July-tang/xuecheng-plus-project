package com.xuecheng.checkcode.service;

import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 验证码接口
 *
 * @author july
 */
public interface CheckCodeService {


    /**
     * 生成验证码
     *
     * @param checkCodeParamsDto 生成验证码参数
     * @return com.xuecheng.checkcode.model.CheckCodeResultDto 验证码结果
     */
    CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

    /**
     * 校验验证码
     *
     * @param key  验证码key
     * @param code 验证码
     * @return boolean
     */
    boolean verify(String key, String code);


    /**
     * 验证码生成器
     */
    interface CheckCodeGenerator {
        /**
         * 验证码生成
         *
         * @param length 验证码长度
         * @return 验证码
         */
        String generate(int length);
    }

    /**
     * key生成器
     */
    interface KeyGenerator {

        /**
         * key生成
         *
         * @param prefix key前缀
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     * 验证码存储器
     */
    interface CheckCodeStore {

        /**
         * 向缓存设置key
         *
         * @param key    key
         * @param value  value
         * @param expire 过期时间,单位秒
         */
        void set(String key, String value, Integer expire);

        /**
         * 获取验证码
         *
         * @param key 验证码key
         * @return 验证码
         */
        String get(String key);

        /**
         * 移除验证码
         *
         * @param key 验证码key
         */
        void remove(String key);
    }
}
