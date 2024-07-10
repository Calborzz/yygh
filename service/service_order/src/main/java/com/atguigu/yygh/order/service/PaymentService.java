package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
    /**
     * 保存交易记录
     * @param order
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    void savePaymentInfo(OrderInfo order, Integer paymentType);

    /**
     * 更新支付状态
     * @param outTradeNo 交易号
     * @param paymentType 支付类型 微信 支付宝
     * @param paramMap 调用微信查询支付状态接口返回map集合
     */
    void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> paramMap);

    /**
     * 通过订单号查询支付记录
     */
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}