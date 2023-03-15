package com.xuecheng.orders.api;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.xuecheng.base.enums.StatusCodeEnum;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.xuecheng.orders.config.AlipayConfig.*;

/**
 * @author july
 */
@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@Controller
public class OrderController {

    @Resource
    OrderService orderService;

    @Value("${pay.APP_ID}")
    String appId;
    @Value("${pay.APP_PRIVATE_KEY}")
    String appPrivateKey;
    @Value("${pay.ALIPAY_PUBLIC_KEY}")
    String alipayPublicKey;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        return orderService.createOrder(UserUtil.getUserId(), addOrderDto);
    }

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestPay(String payNo, HttpServletResponse httpResponse) throws IOException {
        XcPayRecord payRecord = orderService.getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            XueChengPlusException.cast("该二维码已过期，请重新点击支付以获取二维码！");
        }
        if (StatusCodeEnum.RECORD_PAID.getCode().equals(payRecord.getStatus())) {
            XueChengPlusException.cast("该订单已支付，请勿重复支付！");
        }
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, appId, appPrivateKey, "json", CHARSET, alipayPublicKey, SIGN_TYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        alipayRequest.setNotifyUrl(AlipayConfig.notifyUrl);
        //填充业务参数
        StringBuilder bizContent = new StringBuilder().append("{")
                .append("\"out_trade_no\":\"").append(payRecord.getPayNo()).append("\",")
                .append("\"total_amount\":\"").append(payRecord.getTotalPrice()).append("\",")
                .append("\"subject\":\"").append(payRecord.getOrderName()).append("\",")
                .append("\"product_code\":\"QUICK_WAP_PAY\"")
                .append("}");
        alipayRequest.setBizContent(bizContent.toString());
        String form = "";
        try {
            //请求支付宝下单接口,发起http请求
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        //直接将完整的表单html输出到页面
        httpResponse.getWriter().write(form);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    @ApiOperation("查询支付结果")
    @GetMapping("/querypay")
    public void queryPay(HttpServletResponse response) {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, appId, appPrivateKey, FORMAT, CHARSET, alipayPublicKey, SIGN_TYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "1635992524415508480");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse result = null;
        try {
            result = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation("接收支付结果通知")
    @PostMapping("/receivenotify")
    public void receiveNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = new HashMap<>(16);
        for (String name : request.getParameterMap().keySet()) {
            String[] values = request.getParameterMap().get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, CHARSET, SIGN_TYPE);
        //验证失败直接返回
        if (!signVerified) {
            return;
        }
        //商户订单号
        String outTradeNo = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //支付宝交易号
        String tradeNo = new String(request.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //交易状态
        String tradeStatus = new String(request.getParameter("trade_status").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //appid
        String appId = new String(request.getParameter("app_id").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //total_amount
        String totalAmount = new String(request.getParameter("total_amount").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            PayStatusDto payStatusDto = new PayStatusDto();
            payStatusDto.setOut_trade_no(outTradeNo);
            payStatusDto.setTrade_status(tradeStatus);
            payStatusDto.setApp_id(appId);
            payStatusDto.setTrade_no(tradeNo);
            payStatusDto.setTotal_amount(totalAmount);
            orderService.saveAliPayStatus(payStatusDto);
        }
    }
}
