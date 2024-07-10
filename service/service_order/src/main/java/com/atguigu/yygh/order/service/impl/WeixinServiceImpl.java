package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.config.WxpayConfig;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.order.utils.HttpClient;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private WxpayConfig wxpayConfig;

    @Autowired
    private RefundInfoService refundInfoService;

    @Override
    public Map createNative(Long orderId) {
        try {
            //在redis中查询订单
            Map payMap = (Map)redisTemplate.opsForValue().get(orderId.toString());
            //查询订单信息
            OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
            //保存支付交易记录
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());

            //1、设置传递参数
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("appid", wxpayConfig.getAppid()); //公众账号ID
            paramMap.put("mch_id", wxpayConfig.getPartner()); //商户号ID
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
            paramMap.put("body", new DateTime(orderInfo.getReserveDate()).toString("yyyy/MM/dd") + "就诊"+ orderInfo.getDepname()); //商品描述
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", orderInfo.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");//为了测试  0.01元
            paramMap.put("spbill_create_ip", "127.0.0.1"); //设备ip
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify"); //微信回调地址  因为需要公网ip所以先用个假的
            paramMap.put("trade_type", "NATIVE");

            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,wxpayConfig.getPartnerkey()));
            client.setHttps(true); //开启https
            client.post(); //post发送

            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            if(null != resultMap.get("result_code")) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map queryPayStatus(Long orderId) {
        try {
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            //1、封装参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", wxpayConfig.getAppid()); //公众账号ID
            paramMap.put("mch_id", wxpayConfig.getPartner()); //商户号ID
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, wxpayConfig.getPartnerkey()));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、返回
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Boolean refund(Long orderId) {
        //1.查询支付记录
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        //2.创建退款记录
        RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
        if(refundInfo.getRefundStatus().intValue()== RefundStatusEnum.REFUND.getStatus()){
            //如果已经退款就true
            return true;
        }
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
        paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
        paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
//       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+""); //返回金额
//       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+""); //返回金额
        paramMap.put("total_fee","1");
        paramMap.put("refund_fee","1");
        //3.请求微信 进行退款
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //4、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                //如果微信退款成功就保存信息
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}