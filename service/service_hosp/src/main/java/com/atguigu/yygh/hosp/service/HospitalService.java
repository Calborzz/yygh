package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {

    /**
     * 上传医院信息
     * @param paramMap
     */
    void save(Map<String, Object> paramMap);

    /**
     * 获取医院信息
     * @param hoscode
     * @return
     */
    Hospital getByHoscode(String hoscode);

    /**
     * 分页查询医院列表
     * @param pageNum
     * @param limitSize
     * @param hospitalQueryVo
     * @return
     */
    Page<Hospital> getHospitalPage(Integer pageNum, Integer limitSize, HospitalQueryVo hospitalQueryVo);

    /**
     * 更新医院状态
     * @param id
     * @param status
     */
    void updateStatus(String id, Integer status);

    /**
     * 通过id查医院
     * @param id
     * @return
     */
    Hospital getHospitalById(String id);

    /**
     * 通过名字模糊查医院列表
     * @param hosname
     * @return
     */
    List<Hospital> getHospitalByHosname(String hosname);

    /**
     * 查看医院详细内容
     *
     * @param hoscode
     * @return
     */
    Hospital item(String hoscode);

}
