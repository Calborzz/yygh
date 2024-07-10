package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api("微信支付相关API")
@RestController
@RequestMapping("/user/order/weixin/auth")
public class WxpayController {

    @Autowired
    private WeixinService weixinService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrderInfoService orderInfoService;


    @ApiOperation("微信支付二维码")
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable Long orderId){
        Map map = weixinService.createNative(orderId);
        return R.ok().data(map);
    }

    @ApiOperation("查询用户微信支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public R queryPayStatus(@PathVariable Long orderId){
        Map<String,String> map = weixinService.queryPayStatus(orderId);
        if (map==null){
            return R.error().message("支付出错");
        }
        if ("SUCCESS".equals(map.get("trade_state"))){
            //更改订单状态，处理支付结果
            String out_trade_no = map.get("out_trade_no");
            paymentService.paySuccess(out_trade_no, PaymentTypeEnum.WEIXIN.getStatus(), map);
            return R.ok().message("支付成功");
        }
        return R.ok().message("支付中");
    }

    @ApiOperation("取消预约")
    @GetMapping("/cancelOrder/{orderId}")
    public R cancelOrder(@PathVariable Long orderId){
        Boolean flag = orderInfoService.cancelOrder(orderId);
        return R.ok().data("flag",flag);
    }
}
