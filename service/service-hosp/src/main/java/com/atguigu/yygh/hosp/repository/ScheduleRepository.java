package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.hosp.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> getSchedulesByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);

    void removeScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);
}
