package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.model.hosp.Hospital;
import com.atguigu.yygh.hosp.model.hosp.Schedule;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.hosp.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;


    @Override
    public void save(Map<String, Object> switchMap) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(switchMap),Schedule.class);

        String hoscode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        Schedule schduleTarget = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (null != schduleTarget){
            BeanUtils.copyProperties(schedule,schduleTarget);
            scheduleRepository.save(schduleTarget);
        }else{
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }



    }

    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {

        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //create pageable, 0 is the no1
        Pageable pageable = PageRequest.of(page-1,limit,sort);

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);
        schedule.setIsDeleted(0);

        //create matcher, set rules,match query condition
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //create example
        Example<Schedule> example = Example.of(schedule, exampleMatcher);

        //use mongoDB
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);

        return all;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        scheduleRepository.removeScheduleByHoscodeAndHosScheduleId(hoscode,hosScheduleId);

    }
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;

    /**
     * 查询排班规则信息，根据hoscode，depcode
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
        //get all schedule by hoscode and depcode
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //sort and group by workDate
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//matching condition
                Aggregation.group("workDate")//group by workDate
                        .first("workDate").as("workDate")//files of group
                        .count().as("docCount")//count number of doctor
                        .sum("reservedNumber").as("reservedNumber")//count number of reserved
                        .sum("availableNumber").as("availableNumber"),//count number of available
                Aggregation.sort(Sort.Direction.DESC, "workDate"),//sort by workDate
                Aggregation.skip((page - 1) * limit),// enable page func
                Aggregation.limit(limit)
        );

        //调用方法，最终执行
        AggregationResults<BookingScheduleRuleVo> aggResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResults.getMappedResults();

        //就诊医生人数

        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalResults = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalResults.getMappedResults().size();

        //convert date to 星期
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList){
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek =  this.getDayofWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //packing finial data
        Map<String, Object> results = new HashMap<>();

        results.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        results.put("total",total);

        //get name of hospital
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        String hosname = hospital.getHosname();

        Map<String, Object> basemap = new HashMap<>();
        basemap.put("hosname",hosname);
        results.put("baseMap",basemap);



        return results;
    }

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        //get all target object by mongoDB
        List<Schedule> schedules = scheduleRepository
                .getSchedulesByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());

        schedules.stream().forEach(item -> {
            this.packingSchedule(item);
        });

        return schedules;
    }
    @Autowired
    private DepartmentService departmentService;

    //inspect something else, such as hosname, depname, dayOfWeek
    private void packingSchedule(Schedule schedule) {
        String hosname = hospitalService.getByHoscode(schedule.getHoscode()).getHosname();
        String depname = departmentService.getDepnameByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode());
        DateTime dateTime = new DateTime(schedule.getWorkDate());
        String dayofWeek = this.getDayofWeek(dateTime);

        schedule.getParam().put("hosname",hosname);
        schedule.getParam().put("depname",depname);
        schedule.getParam().put("dayOfWeek",dayofWeek);
    }

    //根据日期获取周几数据
    private String getDayofWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;

    }
}
