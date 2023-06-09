package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.hosp.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    Hospital getHospitalByHoscode(String hoscode);

    Hospital getHospitalById(String id);
}
