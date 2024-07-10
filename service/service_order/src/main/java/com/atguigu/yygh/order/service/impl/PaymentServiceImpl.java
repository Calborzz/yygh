package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Transactional
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 保存交易记录
     * @param orderInfo
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderInfo.getId());
        wrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if (count>0)return; //保存过就取消
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId()); //订单号
        paymentInfo.setPaymentType(paymentType); //支付方式
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo()); //对外业务编号
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus()); //支付状态
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);//交易内容
        paymentInfo.setTotalAmount(orderInfo.getAmount());//金额
        baseMapper.insert(paymentInfo);
    }

    @Override
    public void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> paramMap) {
        //1.更新订单状态
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",outTradeNo);
        wrapper.eq("payment_type",paymentType);
        OrderInfo orderInfo = orderInfoService.getOne(wrapper);
        orderInfo.setOrderStatus(PaymentStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        //2.更新支付记录状态
        QueryWrapper<PaymentInfo> wrapperPayment = new QueryWrapper<>();
        wrapperPayment.eq("out_trade_no",outTradeNo);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapperPayment);
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(paramMap.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(paramMap.toString());
        baseMapper.updateById(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId);
        wrapper.eq("payment_type",paymentType);
        return baseMapper.selectOne(wrapper);
    }
}