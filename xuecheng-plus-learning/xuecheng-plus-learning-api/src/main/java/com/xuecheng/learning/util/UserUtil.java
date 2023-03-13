package com.xuecheng.learning.util;

import com.xuecheng.base.exception.XueChengPlusException;
import org.apache.commons.lang.StringUtils;

/**
 * @author july
 */
public class UserUtil {

    public static String getUserId() {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("当前未登录！");
        }
        return user.getId();
    }


    public static Long getCompanyId() {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("当前未登录！");
        }
        String companyId = user.getCompanyId();
        if (StringUtils.isEmpty(companyId)) {
            XueChengPlusException.cast("当前用户未加入机构！");
        }
        return Long.parseLong(companyId);
    }
}
