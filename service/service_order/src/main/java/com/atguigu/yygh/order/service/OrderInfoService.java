package com.atguigu.yygh.order.service;


import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 保存订单
     * @param scheduleId
     * @param patientId
     * @return
     */
    Long saveOrder(String scheduleId, Long patientId);

    /**
     * 用户查看订单分页列表
     * @param pageParam
     * @param orderQueryVo
     * @return
     */
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    /**
     * 查询订单信息
     */
    OrderInfo getOrderInfo(Long id);

    /**
     * 取消预约订单
     * @param orderId
     * @return
     */
    Boolean cancelOrder(Long orderId);

    /**
     * 每天8点定时就诊提醒
     */
    void patientTips();

    /**
     * 订单统计
     */
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
