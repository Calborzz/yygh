package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;


import java.util.List;
import java.util.Map;

public interface DepartmentService {
    /**
     * 上传科室信息
     * @param paramMap
     */
    void save(Map<String, Object> paramMap);

    /**
     * 查询科室分页信息
     * @param page
     * @param limit
     * @param departmentQueryVo
     * @return
     */
    Page<Department> findPage(int page, int limit, DepartmentQueryVo departmentQueryVo);

    /**
     *
     * 删除科室信息
     * @param hoscode
     * @param depcode
     */
    void removeDep(String hoscode,String depcode);


    /**
     * 根据医院编号，查询医院所有科室树形列表
     * @param hoscode
     * @return
     */
    List<DepartmentVo> findDeptTree(String hoscode);

    String getDepName(String hoscode, String depcode);

    Department getDepartment(String hoscode, String depcode);
}
