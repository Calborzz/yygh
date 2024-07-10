package com.atguigu.yygh.hosp.client;

import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-hosp")
public interface HospitalFeignClient {

    /**
     * 获取排班订单信息
     * @param scheduleId
     * @return
     */
    @GetMapping("/admin/hosp/schedule/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId);
}
