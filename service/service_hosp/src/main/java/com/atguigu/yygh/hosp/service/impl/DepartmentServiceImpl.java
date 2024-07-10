package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.mapper.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;

import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    @Override
    public void save(Map<String, Object> paramMap) {
        //转化参数
        String jsonString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);
        if (StringUtils.isEmpty(department.getHoscode())&&StringUtils.isEmpty(department.getDepcode())){
            throw new YyghException(20001,"hoscode或depcode不存在");
        }
        //根据医院号和科室号信息进行查询
        Department dep = departmentRepository.findDepartmentByHoscodeAndDepcode(
                department.getHoscode(), department.getDepcode());
        if (dep==null){ //查不到就添加
            //设置基本参数
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else { //查到就修改
            department.setCreateTime(dep.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            department.setId(dep.getId());
            departmentRepository.save(department);
        }

    }

    @Override
    public Page<Department> findPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //设置分页条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(page-1, limit, sort);
        //封装查询信息
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        department.setIsDeleted(0);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase(true)//忽略大小写
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);//改变默认字符串匹配方式：模糊查询
        //创建实例
        Example<Department> example = Example.of(department, matcher);
        Page<Department> departmentPage = departmentRepository.findAll(example, pageRequest);

        return departmentPage;
    }


    @Override
    public void removeDep(String hoscode, String depcode) {

        Department department = departmentRepository.findDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department!=null){ //存在就删除
            departmentRepository.delete(department);
        }else {
            throw new YyghException(20001,"部门不存在");
        }

    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.findDepartmentByHoscodeAndDepcode(hoscode,depcode);
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        return departmentRepository.findDepartmentByHoscodeAndDepcode(hoscode,depcode).getDepname();
    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //查询hoscode的这家医院的所有科室
        List<Department> departmentList = departmentRepository.findDepartmentByHoscode(hoscode);
        //将所有科室按照大科室进行分类进行分组
        Map<String, List<Department>> bigcodeMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //创建list集合，用于最终数据封装
        List<DepartmentVo> result = new ArrayList<>();
        //循环每个数据并进行封装
        for (Map.Entry<String, List<Department>> entry : bigcodeMap.entrySet()) {
            //封装大科室
            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(entry.getKey()); //大科室编号
            departmentVo.setDepname(entry.getValue().get(0).getDepname()); //大科室名称
            //封装大科室里的小科室
            ArrayList<DepartmentVo> childrenList = new ArrayList<>();
            for (Department department : entry.getValue()) {
                DepartmentVo childDept = new DepartmentVo();
                childDept.setDepcode(department.getDepcode());
                childDept.setDepname(department.getDepname());
                childrenList.add(childDept);
            }
            //将小科室封装到大科室
            departmentVo.setChildren(childrenList);
            //加给最终List
            result.add(departmentVo);
        }
        return result;
    }
}
