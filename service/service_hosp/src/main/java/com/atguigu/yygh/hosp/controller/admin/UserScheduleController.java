package com.atguigu.yygh.hosp.controller.admin;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Api("用户查询排班信息API")
@RestController
@RequestMapping("/user/hosp/schedule/auth")
public class UserScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation("获取可预约排班数据")
    @GetMapping("/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getBookingScheduleRule( @PathVariable Integer page, @PathVariable Integer limit,
            @PathVariable String hoscode, @PathVariable String depcode){
        Map<String, Object> map = scheduleService.getBookingScheduleRule(page,limit,hoscode,depcode);
        return R.ok().data(map);
    }

    @ApiOperation("查看详细的排班信息")
    @GetMapping("/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(@PathVariable String hoscode, @PathVariable String depcode,
                         @PathVariable String workDate){
        List<Schedule> scheduleList = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return R.ok().data("list",scheduleList);
    }

    @ApiOperation("查询排班")
    @GetMapping("/getSchedule/{id}")
    public R getSchedule(@PathVariable String id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        return R.ok().data("schedule",schedule);
    }
}
