package com.atguigu.yygh.hosp.mapper;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {

    Department findDepartmentByHoscodeAndDepcode(String hoscode,String depcode);
    List<Department> findDepartmentByHoscode(String hoscode);


}
