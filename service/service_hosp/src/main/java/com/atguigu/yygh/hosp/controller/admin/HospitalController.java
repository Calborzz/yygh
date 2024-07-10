package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


@Api("医院设置api接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;


    @ApiOperation("查询医院分页信息")
    @GetMapping("/page/{page}/{limit}")
    public R getHospitalPage(@PathVariable Integer page,
                             @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitalPage = hospitalService.getHospitalPage(page, limit, hospitalQueryVo);
        return R.ok().data("list",hospitalPage.getContent())
                .data("total",hospitalPage.getTotalElements())
                .data("page",hospitalPage.getTotalPages());
    }

    @ApiOperation("更新医院上线状态")
    @GetMapping("/updateStatus/{id}/{status}")
    public R updateStatus(@PathVariable String id,@PathVariable Integer status){
        if (status==1||status==0){
            hospitalService.updateStatus(id,status);
            return R.ok();
        }else {
            return R.error().message("status值错误");
        }

    }

    @ApiOperation("获取医院详细信息")
    @GetMapping("/showHospital/{id}")
    public R showHospital(@PathVariable String id){
        //查询医院信息
        Hospital hospital = hospitalService.getHospitalById(id);
        return R.ok().data("hospital",hospital);
    }
}
