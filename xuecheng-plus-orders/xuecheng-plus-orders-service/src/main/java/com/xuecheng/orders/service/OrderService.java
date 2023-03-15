package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;

/**
 * 订单服务相关接口
 *
 * @author july
 */
public interface OrderService {

    /**
     * 创建商品订单
     *
     * @param userId      用户id
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);
}
