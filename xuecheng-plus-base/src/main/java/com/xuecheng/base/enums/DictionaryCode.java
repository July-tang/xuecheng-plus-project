package com.xuecheng.base.enums;

/**
 * @author july
 * @description 课程审核状态
 */
public class DictionaryCode {

    /**
     * 课程收费情况
     */
    public static final String COURSE_FREE = "201000";
    public static final String COURSE_CHARGE = "201001";

    /**
     * 课程审核状态
     */
    public static final String AUDIT_NOT_PASS = "202001";
    public static final String AUDIT_NOT_SUBMIT = "202002";
    public static final String AUDIT_SUBMIT = "202003";
    public static final String AUDIT_PASS = "202004";

    /**
     * 课程发布状态
     */
    public static final String COURSE_NOT_SUBMIT = "203001";
    public static final String COURSE_SUBMIT = "203002";
    public static final String COURSE_OFF = "203003";

    /**
     * 选课类型
     */
    public static final String FREE = "700001";
    public static final String CHARGE = "700002";

    /**
     * 选课状态 [{"code":"701001","desc":"选课成功"},{"code":"701002","desc":"待支付"}]
     */
    public static final String CHOOSE_SUCCESS = "701001";
    public static final String WAIT_PAY = "701002";

    /**
     * 选课学习资格 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     */
    public static final String NORMAL_STUDY = "702001";
    public static final String NOT_CHOOSE_PAY = "702002";
    public static final String EXPIRED = "702003";
}
