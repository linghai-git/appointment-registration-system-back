package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.hosp.model.hosp.Department;
import com.atguigu.yygh.hosp.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.hosp.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save(Map<String, Object> map);

    /**
     *
     * @param page
     * @param limit
     * @param departmentVo  查询条件
     * @return
     */
    Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentVo);

    void removeDepartment(String hoscode, String depcode);

    List<DepartmentVo> getDeptList(String hoscode);


    String getDepnameByHoscodeAndDepcode(String hoscode, String depcode);
}
