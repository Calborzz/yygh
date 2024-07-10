package com.atguigu.yygh.order.client;

import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("service-order")
public interface OrderFeignClient {

    /**
     * 获取订单数据
     */
    @PostMapping("/api/order/orderInfo/auth/inner/getCountMap")
    Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);

}
