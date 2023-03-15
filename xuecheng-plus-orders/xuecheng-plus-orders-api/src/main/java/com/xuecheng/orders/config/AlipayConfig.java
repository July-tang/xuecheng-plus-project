package com.xuecheng.orders.config;

/**
 * 支付宝配置参数
 *
 * @author july
 */
public class AlipayConfig {
    /**
     * 服务器异步通知页面路径
     */
    public static String notifyUrl = " http://rcwai8.natappfree.cc/orders/receivenotify";
    /**
     * 页面跳转同步通知页面路径
      */
    public static String returnUrl = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/return_url.jsp";
    /**
     * 请求网关地址
     */
    public static String URL = "https://openapi.alipaydev.com/gateway.do";
    /**
     * 编码
     */
    public static String CHARSET = "UTF-8";
    /**
     * 返回格式
     */
    public static String FORMAT = "json";
    /**
     * 日志记录目录
     */
    public static String log_path = "/log";
    /**
     * RSA2
     */
    public static String SIGN_TYPE = "RSA2";
}
