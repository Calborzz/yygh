package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.pojo.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api("第三方医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiHospitalController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation("上传医院")
    @PostMapping("/saveHospital")
    public Result save(HttpServletRequest request) {
        //获取map数据并进行转换
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String) map.get("hoscode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001, "hoscode不存在");
        }

        //签名校验
        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) map.get("sign");
        //2 根据传递过来医院编码(hoscode)，查询数据库，查询签名(signKey)
        String signKey = hospitalSetService.getSignKey(hoscode);
        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);
        //4 判断签名是否一致 并判断所有字段是否存在
        if (!hospSign.equals(signKeyMd5) && StringUtils.isEmpty(hospSign) && StringUtils.isEmpty(signKey)) {
            throw new YyghException(20001, "校验失败");
        }

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String) map.get("logoData");
        logoData.replaceAll(" ","+");

        hospitalService.save(map);
        return Result.ok();
    }

    @ApiOperation("查询医院信息")
    @PostMapping("/hospital/show")
    public Result show(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验数据
        String hoscode = (String) map.get("hoscode");
        if (StringUtils.isEmpty(hoscode)){
            throw new YyghException(20001,"hoscode缺失");
        }
        //查询数据
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    @ApiOperation("上传科室信息")
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String,Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        if (map.isEmpty()){
            throw new YyghException(20001,"无科室数据");
        }
        departmentService.save(map);
        return Result.ok();
    }

    @ApiOperation("查询科室信息")
    @PostMapping("/department/list")
    public Result department(HttpServletRequest request){
        Map<String,Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        if (map.isEmpty()){
            throw new YyghException(20001,"无科室数据");
        }
        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");

        int page = StringUtils.isEmpty(map.get("page")) ? 1 : Integer.parseInt((String)map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit")) ? 10 : Integer.parseInt((String)map.get("limit"));
        //签名校验
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        departmentQueryVo.setDepcode(depcode);
        Page<Department> pageModel = departmentService.findPage(page, limit, departmentQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("删除科室信息")
    @PostMapping("/department/remove")
    public Result removeDep(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");
        //校验
        if(StringUtils.isEmpty(hoscode)&&StringUtils.isEmpty(depcode)){
            throw new YyghException(20001,"hoscode或depcode不存在");
        }
        //删除
        departmentService.removeDep(hoscode,depcode);
        return Result.ok();
    }


    @ApiOperation("添加排班信息")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.save(map);
        return Result.ok();
    }

    @ApiOperation("查询排班信息")
    @PostMapping("/schedule/list")
    public Result scheduleList(HttpServletRequest request){
        //获取hoscode和depcode和分页信息（page,limit）
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");
        int page = StringUtils.isEmpty(map.get("page")) ? 1 : Integer.parseInt((String)map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit")) ? 10 : Integer.parseInt((String)map.get("limit"));
        //查询条件
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        //调用服务层的管理方法
        Page<Schedule> resultPage = scheduleService.selectPage(page, limit, scheduleQueryVo);
        return Result.ok(resultPage);
    }

    @ApiOperation("删除排班信息")
    @PostMapping("/schedule/remove")
    public Result removeSchedule(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String)map.get("hoscode");
        String hosScheduleId = (String)map.get("hosScheduleId");
        //校验
        if(StringUtils.isEmpty(hoscode)&&StringUtils.isEmpty(hosScheduleId)){
            throw new YyghException(20001,"hoscode或hosScheduleId不存在");
        }
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

}
