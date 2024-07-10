package com.atguigu.yygh.order.service;

import java.util.Map;

public interface WeixinService {
    /**
     * 根据订单号下单，生成支付二维码链接
     */
    Map createNative(Long orderId);

    /**
     * 查询用户是否付款
     */
    Map queryPayStatus(Long orderId);

    /***
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);
}