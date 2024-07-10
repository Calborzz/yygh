package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper,Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public List<Patient> findAllUserId(Long userId) {
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<Patient> patientList = baseMapper.selectList(wrapper);
        patientList.forEach(this::packPatient); //循环封装
        return patientList;
    }

    @Override
    public Patient getPatientId(Long id) {
        Patient patient = baseMapper.selectById(id);
        this.packPatient(patient);
        return patient;
    }

    //Patient对象里面其他参数封装
    private Patient packPatient(Patient patient) {
        //根据证件编号查名称
        String certificatesTypeString = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), Long.parseLong(patient.getCertificatesType()));
        //查看联系人的证件
        String contactsCertificatesTypeString = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), Long.parseLong(patient.getContactsCertificatesType()));
        //省
        String provinceString = dictFeignClient.getName(Long.parseLong(patient.getProvinceCode()));
        //市
        String cityString = dictFeignClient.getName(Long.parseLong(patient.getCityCode()));
        //区
        String districtString = dictFeignClient.getName(Long.parseLong(patient.getDistrictCode()));
        //封装
        patient.getParam().put("certificatesTypeString",certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString",provinceString);
        patient.getParam().put("cityString",cityString);
        patient.getParam().put("districtString",districtString);
        return patient;
    }
}
