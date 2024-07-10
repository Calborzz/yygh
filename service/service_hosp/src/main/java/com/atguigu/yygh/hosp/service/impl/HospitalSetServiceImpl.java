package com.atguigu.yygh.hosp.service.impl;


import com.atguigu.yygh.common.exception.GlobalExceptionHandler;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.mapper.HospitalRepository;
import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医院设置表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2024-05-25
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {


    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = this.getOne(wrapper);
        if (hospitalSet == null){
            throw new YyghException(20001,"医院不存在");
        }
        return hospitalSet.getSignKey();
    }

}
