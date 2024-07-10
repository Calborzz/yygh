package com.atguigu.yygh.user.client;

import com.atguigu.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-user")
public interface PatientFeignClient {

    //获取就诊人
    @GetMapping("/user/userInfo/patient/inner/get/{id}")
    Patient getPatient(@PathVariable("id") Long id);

}
