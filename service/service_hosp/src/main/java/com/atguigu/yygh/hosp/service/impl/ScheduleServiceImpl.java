package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.mapper.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl extends ServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;


    @Override
    public void save(Map<String, Object> map) {
        //转换数据
        String jsonString = JSONObject.toJSONString(map);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);
        //校验数据
        String hoscode = (String) map.get("hoscode");
        String hosScheduleId = (String) map.get("hosScheduleId");
        if (StringUtils.isEmpty(hoscode) && StringUtils.isEmpty(hosScheduleId)) {
            throw new YyghException(20001, "hoscode或hosScheduleId不存在");
        }
        Schedule sche = scheduleRepository.findScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (sche == null) { //不存在就添加
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        } else { //存在就更新
            schedule.setCreateTime(sche.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            schedule.setId(sche.getId());
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        //将查询条件进行转化
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);

        //设置分页条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(page - 1, limit, sort);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        //调用mongo查询分页信息
        Example<Schedule> example = Example.of(schedule, matcher);
        Page<Schedule> schedulePage = scheduleRepository.findAll(example, pageRequest);
        return schedulePage;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.findScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            scheduleRepository.delete(schedule);
        } else {
            throw new YyghException(20001, "排班信息不存在");
        }

    }

    @Override
    public Map<String, Object> getRuleSchedule(Long page, Long limit, String hoscode, String depcode) {
        //1.根据医院编号和科室编号查询排班信息
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //2.根据工作日进行分组 查询条件
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),//匹配条件
                Aggregation.group("workDate")//分组条件
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"),//排序
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        //查询整个列表的数据
        AggregationResults<BookingScheduleRuleVo> aggregationResults =
                mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregationResults.getMappedResults();

        //分组查询总记录数
        Aggregation totalAgg = Aggregation.newAggregation(Aggregation.match(criteria), Aggregation.group("workDate"));
        //查询每天的数据
        AggregationResults<BookingScheduleRuleVo> scheduleRuleVos =
                mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = scheduleRuleVos.getMappedResults().size();

        //封装时间  今天星期几
        for (BookingScheduleRuleVo vo : bookingScheduleRuleVoList) {
            String dayOfWeek = this.getDayOfWeek(new DateTime(vo.getWorkDate()));
            vo.setDayOfWeek(dayOfWeek);
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        result.put("total", total);

        Hospital hospital = hospitalService.getByHoscode(hoscode);
        HashMap<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hospital.getHosname());
        result.put("baseMap", baseMap);
        return result;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        //获取数据
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        //封装数据
        scheduleList.stream().forEach(this::packageSchedule);
        return scheduleList;
    }

    @Override
    public Schedule getScheduleById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        this.packageSchedule(schedule);
        return schedule;
    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    /**
     * 根据排班id获取预约下单数据实现
     * @param scheduleId
     * @return
     */
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        //返回对象
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //查询排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if(null == schedule) {
            throw new YyghException(20001,"排班不存在");
        }
        //查询医院信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        //查询医院预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //查询部门信息
        Department department = departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode());
        //封装医院、部门和排班数据
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(department.getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止日期
        Integer quitDay = bookingRule.getQuitDay(); //多少天截止
        Date quitDate = new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(); //截止日期
        Date quitTime = this.getDateTime(quitDate, bookingRule.getQuitTime()).toDate();  //截止具体时间
        scheduleOrderVo.setQuitTime(quitTime);
        //预约开始时间
        Date startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime()).toDate();//开始具体时间
        scheduleOrderVo.setStartTime(startTime);
        //预约结束时间
        Date endDate = new DateTime().plusDays(quitDay).toDate(); //结束日期
        Date endTime = this.getDateTime(endDate, bookingRule.getStopTime()).toDate(); //结束具体时间
        scheduleOrderVo.setEndTime(endTime);
        //当天停止挂号时间
        Date stopTime = this.getDateTime(schedule.getWorkDate(), bookingRule.getStopTime()).toDate();
        scheduleOrderVo.setStopTime(stopTime);
        return scheduleOrderVo;
    }

    /**
     * 获取医院排班时间表
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //查询医院信息
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        BookingRule bookingRule = hospital.getBookingRule();//该医院的预约规则
        //获取可预约日期分页数据
        IPage<Date> iPage = this.getListDate(page, limit, bookingRule);
        //当前页可预约日期
        List<Date> dateList = iPage.getRecords();

        //MongoDB查询条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), //查询条件
                Aggregation.group("workDate") //时间分组
                        .first("workDate").as("workDate") //时间
                        .count().as("docCount") //医生数量
                        .sum("availableNumber").as("availableNumber") //剩余可预约数
                        .sum("reservedNumber").as("reservedNumber"), //总的可预约数
                Aggregation.sort(Sort.Direction.ASC, "workDate")
        );
        //查询医院可以预约数据
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> list = aggregate.getMappedResults();

        //按照时间进行分类
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = list.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));

        int size = dateList.size();
        ArrayList<BookingScheduleRuleVo> resultList = new ArrayList<>();
        //设置每天的状态和数据
        for (int i = 0; i < size; i++) {
            Date date = dateList.get(i); //当天时间
            //查询排班信息
            BookingScheduleRuleVo scheduleRuleVo = scheduleVoMap.get(dateList.get(i));
            //该天没排班
            if (scheduleRuleVo == null) {
                scheduleRuleVo = new BookingScheduleRuleVo();
                scheduleRuleVo.setDocCount(0);//就诊医生人数
                scheduleRuleVo.setAvailableNumber(-1);//科室剩余预约数  -1表示无号
                scheduleRuleVo.setStatus(0);
            }
            scheduleRuleVo.setWorkDate(date);
            scheduleRuleVo.setWorkDateMd(date);
            scheduleRuleVo.setDayOfWeek(this.getDayOfWeek(new DateTime(date)));//计算当前预约日期为周几
            scheduleRuleVo.setStatus(0);
            //如果的第一页的第一条
            if (i == 0 && page == 1) {
                //停止预约时间
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) { //错过时间
                    scheduleRuleVo.setStatus(-1); //暂停预约
                }
            }
            //如果是最后一页的最后一条 显示即将预约
            if (i == size - 1 && page == iPage.getPages()) {
                scheduleRuleVo.setStatus(1);
            }
            resultList.add(scheduleRuleVo);
        }
        //排班数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("bookingScheduleList", resultList); //排班数据
        map.put("total", iPage.getTotal()); //总记录数
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hospitalService.getByHoscode(hoscode).getHosname());//医院名称
        Department department = departmentService.getDepartment(hoscode, depcode);//科室
        baseMap.put("bigname", department.getBigname());//大科室名称
        baseMap.put("depname", department.getDepname());//科室名称
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));//月
        baseMap.put("releaseTime", bookingRule.getReleaseTime());//放号时间
        baseMap.put("stopTime", bookingRule.getStopTime());//停号时间
        map.put("baseMap", baseMap);
        return map;
    }

    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //判断当前时间是否超过当天预约时间，超过就只能预约下一天
        Integer cycle = bookingRule.getCycle(); //预约周期
        String releaseTime = bookingRule.getReleaseTime(); //结束时间
        DateTime dateTime = this.getDateTime(new Date(), releaseTime); //拼接成当天的时间
        if (dateTime.isBeforeNow()) {  //判断dateTime是否超过现在的时间
            cycle++;
        }
        //所有时间列表(1-10页)
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //转换时间
            dateList.add(new DateTime(new DateTime().plusDays(i).toString("yyyy-MM-dd")).toDate());
        }
        //当前页的时间列表(只显示7页)
        int start = (page - 1) * limit;
        int end = start + limit;
        if (end > dateList.size()) {
            end = dateList.size();
        }
        ArrayList<Date> dates = new ArrayList<>();
        for (int i = start; i < end; i++) {
            dates.add(dateList.get(i));
        }
        //返回分页
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> resultPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, dateList.size());
        resultPage.setRecords(dates);
        return resultPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    /**
     * 封装排班详情其他值 医院名称、科室名称、日期对应星期
     */
    private void packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname", hospitalService.getByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        schedule.getParam().put("depname", departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    /**
     * 根据日期获取周几数据
     *
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
