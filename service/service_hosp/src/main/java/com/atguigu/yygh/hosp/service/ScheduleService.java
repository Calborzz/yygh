package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    /**
     * 保存排班信息
     * @param map
     */
    void save(Map<String, Object> map);

    /**
     * 查询排班数据分页
     * @param page
     * @param limit
     * @param scheduleQueryVo
     * @return
     */
    Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    /**
     * 根据排班编号删除排班
     * @param hoscode
     * @param hosScheduleId
     */
    void remove(String hoscode, String hosScheduleId);

    /**
     * 查询排班时间表
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    Map<String, Object> getRuleSchedule(Long page, Long limit, String hoscode, String depcode);



    /**
     * 根据医院编号 、科室编号和工作日期，查询排班详细信息
     * @param hoscode
     * @param depcode
     * @param workDate
     * @return
     */
    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);


    /**
     * 获取排班可预约日期数据
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     */
    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    /**
     * 获取排班信息
     * @param id
     * @return
     */
    Schedule getScheduleById(String id);

    /**
     * 内部查看排班详细信息
     * @param scheduleId
     * @return
     */
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    /**
     * 修改排班
     */
    void update(Schedule schedule);
}
