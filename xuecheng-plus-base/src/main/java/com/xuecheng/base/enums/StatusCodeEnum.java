package com.xuecheng.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态码字典
 *
 * @author july
 */
@Getter
@AllArgsConstructor
public enum StatusCodeEnum {

    /**
     * 课程收费情况
     */
    FREE("201000", "免费"),
    CHARGE("201001", "收费"),

    /**
     * 课程审核状态
     */
    AUDIT_NOT_PASS("202001", "审核未通过"),
    AUDIT_NOT_SUBMIT("202002", "未提交"),
    AUDIT_SUBMIT("202003", "已提交"),
    AUDIT_PASS("202004", "审核通过"),

    /**
     * 课程发布状态
     */
    COURSE_NOT_SUBMIT("203001", "未发布"),
    COURSE_SUBMIT("203002", "已发布"),
    COURSE_OFF("203003", "下线"),

    /**
     * 订单交易类型状态
     */
    ORDER_UNPAID("600001","未支付"),
    ORDER_PAID("600002", "已支付"),
    ORDER_CLOSED("600003", "已关闭"),
    ORDER_REFUNDED("600004", "已退款"),
    ORDER_FINISHED("600005", "已完成"),

    /**
     * 支付记录交易状态 ,{"code":"601003","desc":"已退款"}]
     */
    RECORD_UNPAID("601001","未支付"),
    RECORD_PAID("601002", "已支付"),
    RECORD_REFUNDED("601003", "已退款"),

    /**
     * 选课类型
     */
    FREE_COURSE("700001", "免费课程"),
    CHARGE_COURSE("700002", "收费课程"),

    /**
     * 选课状态
     */
    CHOOSE_SUCCESS("701001", "选课成功"),
    WAIT_PAY("701002", "待支付"),

    /**
     * 选课学习资格
     */
    NORMAL_STUDY("702001", "正常学习"),
    NOT_CHOOSE_PAY("702002", "没有选课或选课后没有支付"),
    EXPIRED("702003", "已过期需要申请续期或重新支付");

    private final String code;
    private final String description;
}
