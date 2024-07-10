package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netflix.client.http.HttpRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Api("订单Api")
@RestController
@RequestMapping("/api/order/orderInfo/auth")
public class OrderController {

    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation("保存订单")
    @PostMapping("/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId,@PathVariable Long patientId){
        Long orderId = orderInfoService.saveOrder(scheduleId, patientId);
        return R.ok().data("orderId",orderId);
    }

    @ApiOperation("用户查询订单分页信息")
    @GetMapping("/selectPage/{page}/{limit}")
    public R selectPage(@PathVariable Long page, @PathVariable Long limit,
                        OrderQueryVo orderQueryVo, @RequestHeader String token){
        orderQueryVo.setUserId(JwtHelper.getUserId(token));
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);
        IPage<OrderInfo> orderInfoIPage = orderInfoService.selectPage(orderInfoPage, orderQueryVo);
        return R.ok().data("page",orderInfoIPage);
    }

    @ApiOperation("获取订单状态")
    @GetMapping("/getStatusList")
    public R getStatusList(){
        return R.ok().data("statusList", OrderStatusEnum.getStatusList());
    }

    @ApiOperation("查看订单详细")
    @GetMapping("/getOrders/{orderId}")
    public R getOrders(@PathVariable Long orderId){
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        return R.ok().data("orderInfo", orderInfo);
    }

    @ApiOperation("获取订单统计数据")
    @PostMapping("/inner/getCountMap")
    public Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo){
        return orderInfoService.getCountMap(orderCountQueryVo);
    }


}
