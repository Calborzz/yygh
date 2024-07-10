package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.hosp.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.mq.model.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.HttpRequestHelper;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Autowired
    private HospitalFeignClient hospitalFeignClient;
    @Autowired
    private PatientFeignClient patientFeignClient;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private WeixinService weixinService;
    @Autowired
    private PaymentService paymentService;

    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //1.获取排班数据
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        DateTime stopTime = new DateTime(scheduleOrderVo.getStopTime());
        //如果超出预约时间就取消订单
        if (stopTime.isBeforeNow()) {
            throw new YyghException(20001, "已停止挂号");
        }
        //2.获取就诊人数据
        Patient patient = patientFeignClient.getPatient(patientId);
        //如果预约过了，就不能再预约了
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", patient.getUserId());
        wrapper.eq("hoscode", scheduleOrderVo.getHoscode());
        wrapper.eq("depcode", scheduleOrderVo.getDepcode());
        wrapper.eq("hos_schedule_id", scheduleOrderVo.getHosScheduleId());
        if (baseMapper.selectOne(wrapper) != null) {
            //已经预约过
            throw new YyghException(20001, "该患者已经预约过了！");
        }
        //3.请求第三方医院判断能否进行挂号
        //封装数据
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", scheduleOrderVo.getHoscode());
        paramMap.put("depcode", scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate", new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount", scheduleOrderVo.getAmount()); //挂号费用
        //请求第三方
        JSONObject resultJson = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");

        //4 如果医院接口返回成功，添加上面三部分数据到数据库
        if (resultJson != null && resultJson.getInteger("code") == 200) {
            //创建订单并封装信息
            OrderInfo orderInfo = new OrderInfo();
            String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);//订单号
            BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
            orderInfo.setUserId(patient.getUserId());
            orderInfo.setPatientId(patientId);
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
            //设置添加数据--医院接口返回数据
            JSONObject data = resultJson.getJSONObject("data");
            String hosRecordId = data.getString("hosRecordId");//预约记录唯一标识（医院预约记录主键）
            Integer number = data.getInteger("number");//预约序号
            String fetchTime = data.getString("fetchTime");//取号时间
            String fetchAddress = data.getString("fetchAddress");//取号地址
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            //调用保存方法
            baseMapper.insert(orderInfo);

            //5 根据医院返回数据，更新排班数量
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setAvailableNumber(data.getInteger("availableNumber"));//排班剩余预约数
            orderMqVo.setReservedNumber(data.getInteger("reservedNumber"));//排班可预约数
            //将数据发给rabbitmq
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

            //6.给用户返回短信
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(patient.getPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                    + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);
            //将数据发给rabbitmq
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);

            //7 返回订单号
            return orderInfo.getId();
        } else {
            throw new YyghException(20001, "预约失败或预约已满！");
        }
    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        Long userId = orderQueryVo.getUserId(); //用户id
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();

        if (StringUtils.isEmpty(userId)) {
            throw new YyghException(20001, "用户失效请重新登录");
        }
        wrapper.eq("user_id", orderQueryVo.getUserId());
        //非空就进行模糊查询
        if (!StringUtils.isEmpty(name)) {
            wrapper.like("hosname", name);
        }
        if (!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id", patientId);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status", orderStatus);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date", reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        //查询结果
        Page<OrderInfo> orderInfoPage = baseMapper.selectPage(pageParam, wrapper);
        //对结果进行包装
        orderInfoPage.getRecords().stream().forEach(this::packOrderInfo);
        return orderInfoPage;
    }

    @Override
    public OrderInfo getOrderInfo(Long id) {
        OrderInfo orderInfo = baseMapper.selectById(id);
        return this.packOrderInfo(orderInfo);
    }

    /**
     * 取消预约
     *
     * @param orderId
     * @return
     */
    @Override
    public Boolean cancelOrder(Long orderId) {
        OrderInfo orderInfo = this.getById(orderId);
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        //1.判断是否超过退款时间
        if (quitTime.isBeforeNow()) {
            throw new YyghException(20001, "已过取消预约时间");
        }
        //2.向第三方医院发送消息
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        paramMap.put("hosRecordId", orderInfo.getHosRecordId());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        paramMap.put("sign", "");
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/updateCancelStatus");
        if (result.getInteger("code") != 200) {
            throw new YyghException(result.getInteger("code"), result.getString("message"));
        } else {
            //4.判断是否已经付款
            if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                //已经付款，就退款
                Boolean refund = weixinService.refund(orderId);
                if (!refund) { //如果失败
                    throw new YyghException(20001, "微信退款错误");
                }
            }
            //5.更改订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            this.updateById(orderInfo);
            //6.更新支付记录状态
            QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("order_id", orderId);
            PaymentInfo paymentInfo = paymentService.getOne(wrapper);
            paymentInfo.setPaymentStatus(-1);//退款
            paymentInfo.setUpdateTime(new Date());
            paymentService.updateById(paymentInfo);

            //7.发送mq信息 更新预约数
            OrderMqVo orderMqVo = new OrderMqVo();
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
            //发送mq信息 短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            //msmVo.setParam();//退款内容
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
        return true;
    }

    @Override
    public void patientTips() {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("reserve_date", new DateTime().toString("yyyy-MM-dd"));
        wrapper.ne("order_status", -1);
        List<OrderInfo> orderInfoList = baseMapper.selectList(wrapper);
        for (OrderInfo orderInfo : orderInfoList) {
            //封装发送信息
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            //向mq发送消息
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }

    }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        HashMap<String, Object> hashMap = new HashMap<>();
        List<OrderCountVo> list = baseMapper.selectOrderCount(orderCountQueryVo);
        //按照日期进行分类
        List<String> dataList = list.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        //按照数量进行分类
        List<Integer> countList = list.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        hashMap.put("dataList",dataList);
        hashMap.put("countList",countList);
        return hashMap;
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

}
