package com.atguigu.yygh.mq.model;

public class MqConst {
    /**
     * 预约下单
     */
    public static final String EXCHANGE_DIRECT_ORDER = "exchange.direct.order"; //交换机
    public static final String ROUTING_ORDER = "order"; //路由
    public static final String QUEUE_ORDER  = "queue.order"; //队列
    
    /**
     * 短信
     */
    public static final String EXCHANGE_DIRECT_MSM = "exchange.direct.msm"; //交换机
    public static final String ROUTING_MSM_ITEM = "msm.item"; //路由
    public static final String QUEUE_MSM_ITEM  = "queue.msm.item"; //队列

    /**
     * 定时任务
     */
    public static final String EXCHANGE_DIRECT_TASK = "exchange.direct.task";
    public static final String ROUTING_TASK_8 = "task.8";
    public static final String QUEUE_TASK_8 = "queue.task.8";    //队列
}