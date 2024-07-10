package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.client.DictFeignClient;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.mapper.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> paramMap) {
        //将数据转换为hospital对象
        String json = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(json, Hospital.class);
        //查询该数据是否已经存在过
        Hospital target = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());
        if (target == null) { //不存在就添加
            //将数据进行封装
            hospital.setUpdateTime(new Date());
            hospital.setCreateTime(new Date());
            hospital.setStatus(0); //默认未上线
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        } else { //存在就修改
            hospital.setId(target.getId());
            hospital.setCreateTime(target.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setStatus(target.getStatus());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> getHospitalPage(Integer pageNum, Integer limitSize, HospitalQueryVo hospitalQueryVo) {
        //模糊查询条件
        Hospital hospital = new Hospital();
        hospital.setIsDeleted(0);
        //医院名称
        if (!StringUtils.isEmpty(hospitalQueryVo.getHosname())) {
            hospital.setHosname(hospitalQueryVo.getHosname());
        }
        //省份
        if (!StringUtils.isEmpty(hospitalQueryVo.getProvinceCode())) {
            hospital.setProvinceCode(hospitalQueryVo.getProvinceCode());
        }
        //城市
        if (!StringUtils.isEmpty(hospitalQueryVo.getCityCode())) {
            hospital.setCityCode(hospitalQueryVo.getCityCode());
        }

        //设置分页条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(pageNum - 1, limitSize, sort);
        //设置查询方法 (模糊查询)
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase(true)
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        //进行条件分页查询
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> page = hospitalRepository.findAll(example, pageRequest);

        //将数据的省市区编号进行查询名称并封装到数据里
        page.getContent().parallelStream().forEach(item -> this.packHospital(item));
        return page;
    }

    /**
     * 封装数据
     *
     * @param hospital
     * @return
     */
    private Hospital packHospital(Hospital hospital) {
        //获取名称
        String province = dictFeignClient.getName("", Long.parseLong(hospital.getProvinceCode()));
        String city = dictFeignClient.getName("", Long.parseLong(hospital.getProvinceCode()));
        String districtString = dictFeignClient.getName("", Long.parseLong(hospital.getDistrictCode()));
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), Long.parseLong(hospital.getHostype()));

        //放入hospital的其他参数里
        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("address", province + city + districtString + hospital.getAddress());
        return hospital;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        Hospital hospital = hospitalRepository.findById(id).get();
        if (hospital == null) {
            throw new YyghException(20001,"不存在该医院");
        }
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    @Override
    public List<Hospital> getHospitalByHosname(String hosname) {
        return hospitalRepository.getHospitalByHosnameLike(hosname);
    }

    @Override
    public Hospital getHospitalById(String id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        //封装一写基本数据
        this.packHospital(hospital);
        if (hospital==null){
            throw new YyghException(20001,"不存在该医院");
        }
        return hospital;
    }

    @Override
    public Hospital item(String hoscode) {

        //获取医院信息并进行封装
        Hospital hospital = this.packHospital(this.getByHoscode(hoscode));
        return hospital;
    }
}
