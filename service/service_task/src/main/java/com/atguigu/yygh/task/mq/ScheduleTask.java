package com.atguigu.yygh.task.mq;

import com.atguigu.yygh.mq.model.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableScheduling
public class ScheduleTask {
    @Autowired
    private RabbitService rabbitService;

    @Scheduled(cron = "0 0 8 * * ?")
//    @Scheduled(cron = "0/50 * * * * ?") 测试
    public void task1(){
        System.out.println(new Date());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"");
    }
}
