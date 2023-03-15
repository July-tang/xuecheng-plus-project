package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

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

    /**
     * 查询支付交易记录
     *
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     */
    XcPayRecord getPayRecordByPayNo(String payNo);

    /**
     * 保存支付宝支付结果
     *
     * @param payStatusDto  支付结果信息
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);
}
