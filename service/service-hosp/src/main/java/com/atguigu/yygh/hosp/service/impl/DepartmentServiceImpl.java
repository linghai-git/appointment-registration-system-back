package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.model.hosp.Department;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.hosp.vo.hosp.DepartmentVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> map) {

        //将map集合转换成字符串，接着转换成object
        Department department = JSONObject.parseObject(JSONObject.toJSONString(map),Department.class);

        Department targetDepartment = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
        if (null != targetDepartment){
            BeanUtils.copyProperties(department,targetDepartment,Department.class);
            departmentRepository.save(targetDepartment);
        }else{
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }

    }

    @Override
    public Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentVo) {
        //create sort
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");// 0 is the no1 page

        //create pageable
        Pageable pageable = PageRequest.of(page-1,limit,sort);

        Department department = new Department();
        BeanUtils.copyProperties(departmentVo,department);
        department.setIsDeleted(0);

        //create example matcher, match query condition and set rules
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase(true)//ignore case
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);//match default char

        //create example
        Example<Department> example = Example.of(department, matcher);

        Page<Department> pageList =  departmentRepository.findAll(example,pageable);
        return pageList;
    }

    @Override
    public void removeDepartment(String hoscode, String depcode) {
        //use mongoDB remove
        departmentRepository.removeDepartmentByHoscodeAndDepcode( hoscode,  depcode);
    }

    @Override
    public List<DepartmentVo> getDeptList(String hoscode) {
        //create packing List
        List<DepartmentVo> vos = new ArrayList<>();

        //query 所有科室信息 by hoscode
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example<Department> of = Example.of(departmentQuery);
        List<Department> departmentList  = departmentRepository.findAll(of);


        //根据大科室编号  bigcode 分组，获取每个大科室里面下级子科室
        Map<String, List<Department>> departmentMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));

        //foreach 遍历封装
        for(Map.Entry<String,List<Department>> entry: departmentMap.entrySet()){
            //先封装大科室
            String bigCode = entry.getKey();//大科室编号

            List<Department> departmentList1 = entry.getValue();//大科室下的小科室
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigCode);
            //can't fix 0
            departmentVo1.setDepname(departmentList1.get(0).getBigname());

            //封装大科室下的小科室
            ArrayList<DepartmentVo> departmentVo2 = new ArrayList<>();
            for (Department department: departmentList1){
                DepartmentVo departmentVo3 = new DepartmentVo();
                departmentVo3.setDepname(department.getDepname());
                departmentVo3.setDepcode(department.getDepcode());
                departmentVo2.add(departmentVo3);
            }

            departmentVo1.setChildren(departmentVo2);
            vos.add(departmentVo1);
        }

        return vos;
    }

    @Override
    public String getDepnameByHoscodeAndDepcode(String hoscode, String depcode) {
        String depname = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode).getDepname();
        return depname;
    }


}
