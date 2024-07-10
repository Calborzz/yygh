package com.atguigu.yygh.statistics.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.order.client.OrderFeignClient;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api("图像化数据Api")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("/getCountMap")
    public R getCountMap(OrderCountQueryVo orderCountQueryVo){
        Map<String, Object> countMap = orderFeignClient.getCountMap(orderCountQueryVo);
        return R.ok().data(countMap);
    }
}
