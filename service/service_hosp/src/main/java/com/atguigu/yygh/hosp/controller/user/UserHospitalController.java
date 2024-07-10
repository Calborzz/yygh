package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("前台医院api接口")
@RestController
@RequestMapping("/user/hosp/hospital")
public class UserHospitalController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation("获取医院列表")
    @GetMapping("/list")
    public R getHospitalList(HospitalQueryVo hospitalQueryVo){
        Page<Hospital> pageList = hospitalService.getHospitalPage(1, 50000, hospitalQueryVo);
        return R.ok().data("list",pageList.getContent());
    }

    @ApiOperation("通过名字查医院")
    @GetMapping("/list/{hosname}")
    public R getHospitalByName(@PathVariable String hosname){
        List<Hospital> hospitalList = hospitalService.getHospitalByHosname(hosname);
        return R.ok().data("list",hospitalList);
    }

    @ApiOperation("查询医院详细")
    @GetMapping("/{hoscode}")
    public R getHospital(@PathVariable String hoscode){
        Hospital hospital = hospitalService.item(hoscode);
        return R.ok().data("hospital",hospital);
    }

    @ApiOperation("查询科室通过医院号")
    @GetMapping("/department/{hoscode}")
    public R getDepartments(@PathVariable String hoscode){
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return R.ok().data("list",list);
    }



}
