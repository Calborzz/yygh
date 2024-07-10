package com.atguigu.yygh.hosp.service;


import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 医院设置表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2024-05-25
 */
public interface HospitalSetService extends IService<HospitalSet> {
    /**
     * 获取签名key
     * @param hoscode
     * @return
     */
    String getSignKey(String hoscode);
}
