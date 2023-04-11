package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.model.hosp.Department;
import com.atguigu.yygh.hosp.model.hosp.Schedule;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "科室管理接口")
@RestController
@RequestMapping("/admin/hosp/department")

public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;



        //根据医院编号，查询医院所有科室列表
    @ApiOperation(value = "查询医院所有科室列表")
    @GetMapping("getDeptList/{hoscode}")
    public Result getDeptList(@PathVariable String hoscode) {

    List<DepartmentVo> list = departmentService.getDeptList(hoscode);
    return Result.ok(list);

    }
}
