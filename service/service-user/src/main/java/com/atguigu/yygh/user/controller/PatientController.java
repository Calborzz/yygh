package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import com.netflix.client.http.HttpRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api("就诊人Api")
@RestController
@RequestMapping("/user/userInfo/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @ApiOperation("获取所有就诊人信息")
    @GetMapping("/auth/findAll")
    public R findAll(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> patientList = patientService.findAllUserId(userId);
        return R.ok().data("list",patientList);
    }

    @ApiOperation("获取就诊人信息")
    @GetMapping("/auth/get/{id}")
    public R getById(@PathVariable Long id){
        Patient patient = patientService.getPatientId(id);
        return R.ok().data("patient",patient);
    }

    @ApiOperation("内部获取就诊人信息")
    @GetMapping("/inner/get/{id}")
    public Patient getPatient(@PathVariable Long id){
        return patientService.getById(id);
    }

    @PostMapping("/auth/save")
    public R save(@RequestBody Patient patient,HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    @PutMapping("/auth/update")
    public R update(@RequestBody Patient patient,HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setId(userId);
        patientService.updateById(patient);
        return R.ok();
    }

    @DeleteMapping("/auth/remove/{id}")
    public R remove(@PathVariable Long id){
        patientService.removeById(id);
        return R.ok();
    }
}
