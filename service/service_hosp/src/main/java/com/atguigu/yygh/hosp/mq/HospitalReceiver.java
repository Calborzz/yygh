package com.atguigu.yygh.hosp.mq;

import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.mq.model.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import io.lettuce.core.dynamic.annotation.Key;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER,durable = "true"),
            exchange = @Exchange(MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))//rabbit监听器
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel){
        if(null != orderMqVo.getAvailableNumber()) {
            //更新排班
            Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            scheduleService.update(schedule);
        } else {
            //取消预约更新预约数
            Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
            schedule.setAvailableNumber(schedule.getAvailableNumber().intValue() + 1); //可预约数加1
            scheduleService.update(schedule);
        }
    }
}
