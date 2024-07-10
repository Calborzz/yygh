package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api("排班api接口")
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation("获取医院排班时间表")
    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getScheduleRule(@PathVariable Long page,@PathVariable Long limit,
                             @PathVariable String hoscode,@PathVariable String depcode){
        Map<String,Object> map = scheduleService.getRuleSchedule(page,limit,hoscode,depcode);
        return R.ok().data(map);
    }

    @ApiOperation("查询排班详细信息")
    @GetMapping("/getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail( @PathVariable String hoscode,
                                @PathVariable String depcode,
                                @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getDetailSchedule(hoscode,depcode,workDate);
        return R.ok().data("list",list);
    }

    @ApiOperation("内部查询排班信息")
    @GetMapping("/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId){
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

}
