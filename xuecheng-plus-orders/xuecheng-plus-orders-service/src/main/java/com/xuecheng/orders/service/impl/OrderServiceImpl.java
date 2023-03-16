package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.enums.StatusCodeEnum;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.config.RabbitMqConfig;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author july
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    XcOrdersMapper ordersMapper;

    @Resource
    XcPayRecordMapper payRecordMapper;

    @Resource
    XcOrdersGoodsMapper ordersGoodsMapper;

    @Resource
    MqMessageService mqMessageService;

    @Resource
    OrderServiceImpl proxy;

    @Value("${pay.qrUrl}")
    String qrUrl;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        //添加商品订单
        XcOrders xcOrder = proxy.saveXcOrders(userId, addOrderDto);
        //添加支付交易记录
        XcPayRecord payRecord = proxy.createPayRecord(xcOrder);
        //生成二维码
        String qrCode = null;
        String url = String.format(qrUrl, payRecord.getPayNo());
        try {
            //url要可以被模拟器访问到，url为下单接口(稍后定义)
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecordDto, payRecord);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayNo(String payNo) {
        return payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
    }

    /**
     * 保存订单信息
     *
     * @param userId      用户id
     * @param addOrderDto 新增订单dto
     * @return XcOrders 订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        XcOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (order != null) {
            return order;
        }
        //插入订单表
        order = new XcOrders();
        long orderId = IdWorkerUtils.getInstance().nextId();
        BeanUtils.copyProperties(addOrderDto, order);
        order.setId(orderId);
        order.setCreateDate(LocalDateTime.now());
        order.setStatus(StatusCodeEnum.ORDER_UNPAID.getCode());
        order.setUserId(userId);
        if (ordersMapper.insert(order) <= 0) {
            XueChengPlusException.cast("新增订单失败！");
        }
        //插入订单明细表
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(addOrderDto.getOrderDetail(), XcOrdersGoods.class);
        xcOrdersGoods.forEach(good -> {
            XcOrdersGoods xcOrdersGood = new XcOrdersGoods();
            BeanUtils.copyProperties(good, xcOrdersGood);
            xcOrdersGood.setOrderId(orderId);
            if (ordersGoodsMapper.insert(xcOrdersGood) <= 0) {
                XueChengPlusException.cast("新增订单失败！");
            }
        });
        return order;
    }

    /**
     * 保存支付记录
     *
     * @param orders 订单
     * @return XcPayRecord 支付记录
     */
    @Transactional(rollbackFor = Exception.class)
    public XcPayRecord createPayRecord(XcOrders orders) {
        if (StatusCodeEnum.ORDER_PAID.getCode().equals(orders.getStatus())) {
            XueChengPlusException.cast("该订单已支付！");
        }
        XcPayRecord payRecord = new XcPayRecord();
        //生成支付交易流水号
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId());
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus(StatusCodeEnum.RECORD_UNPAID.getCode());
        payRecord.setUserId(orders.getUserId());
        if (payRecordMapper.insert(payRecord) <= 0) {
            XueChengPlusException.cast("拉起支付失败！");
        }
        return payRecord;
    }

    /**
     * 保存alipay支付结果
     *
     * @param payStatusDto dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //支付宝返回结果为支付成功才需要保存
        if (!"TRADE_SUCCESS".equals(payStatusDto.getTrade_status())) {
            return;
        }
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecord = proxy.getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            XueChengPlusException.cast("系统出错，支付记录不存在！");
        }
        if (!StatusCodeEnum.RECORD_UNPAID.getCode().equals(payRecord.getStatus())) {
            XueChengPlusException.cast("该订单已被支付！");
        }
        payRecord.setStatus(StatusCodeEnum.RECORD_PAID.getCode());
        payRecord.setOutPayChannel("Alipay");
        payRecord.setPaySuccessTime(LocalDateTime.now());
        if (payRecordMapper.updateById(payRecord) <= 0) {
            XueChengPlusException.cast("支付记录更新失败！");
        }
        Long orderId = payRecord.getOrderId();
        XcOrders order = ordersMapper.selectById(orderId);
        if (order == null) {
            XueChengPlusException.cast("根据交易记录找不到订单!");
        }
        order.setStatus(StatusCodeEnum.ORDER_PAID.getCode());
        if (ordersMapper.updateById(order) <= 0) {
            XueChengPlusException.cast("订单结果更新失败！");
        }
        mqMessageService.addMessage(RabbitMqConfig.PAY_NOTIFY, order.getOutBusinessId(), null, null);
    }

    /**
     * 根据业务id查询订单
     *
     * @param businessId 业务id
     * @return XcOrders 订单信息
     */
    public XcOrders getOrderByBusinessId(String businessId) {
        return ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
    }
}
