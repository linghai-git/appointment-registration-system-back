package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.hosp.enums.DictEnum;
import com.atguigu.yygh.hosp.model.hosp.Hospital;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.vo.hosp.HospitalQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HospitalServiceImpl implements HospitalService{
    @Autowired
    private HospitalRepository hospitalRepository;
    @Override
    public void save(Map<String, Object> paramMap) {
        log.info(JSONObject.toJSONString(paramMap));
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Hospital.class);
        //判断是否存在
        Hospital targetHospital = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());
        if(null != targetHospital) {
            hospital.setStatus(targetHospital.getStatus());
            hospital.setCreateTime(targetHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        } else {
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode);
    }

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //create pageable
        Pageable pageable = PageRequest.of(page-1,limit,sort);

        //create object
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);// that is a little redundant
        //create matcher
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //match
        Example<Hospital> example = Example.of(hospital, exampleMatcher);

        Page<Hospital> all = hospitalRepository.findAll(example, pageable);


        all.getContent().stream().forEach(
                item -> {
                    this.setHospitalParams(item);
                }
        );

        return all;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if(status.intValue() == 0 || status.intValue() == 1) {
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }

    }

    @Override
    public Map<String, Object> show(String id) {

        HashMap<String, Object> hashMap = new HashMap<>();

        Hospital hospital = hospitalRepository.findById(id).get();
//        Hospital hospital = hospitalRepository.getHospitalById(id);
        Hospital hospital1 = this.setHospitalParams(hospital);
        hashMap.put("hospital",hospital1);

        hashMap.put("bookingRule",hospital1.getBookingRule());
        hospital1.setBookingRule(null);
        return hashMap;
    }

    private Hospital setHospitalParams(Hospital item) {


        //get hostType
        String hostType = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), item.getHostype());

        //get address
        String province = dictFeignClient.getName(item.getProvinceCode());
        String city = dictFeignClient.getName(item.getCityCode());
        String district = dictFeignClient.getName(item.getDistrictCode());

        //packaging
        item.getParam().put("hostType", hostType);
        item.getParam().put("address", province+city+district);

        return item;
    }

}
