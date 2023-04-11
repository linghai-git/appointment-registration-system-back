package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.hosp.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {


    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

//    void removeByHoscodeAndDepcode(String hoscode, String depcode);
    void removeDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

}
