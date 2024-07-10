package com.atguigu.yygh.order.receiver;

import com.atguigu.yygh.mq.model.MqConst;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderReceiver {

    private OrderInfoService orderInfoService;


    //每天八点 自动调用此方法
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8,durable = "true"),
            exchange = @Exchange(MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_8}
    ))
    public void patientTips(Message message, Channel channel) throws IOException {
        System.out.println("每天8点自动执行");
        orderInfoService.patientTips();
    }
}
