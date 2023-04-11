package com.atguigu.yygh.hosp.controller.api;


import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.util.MD5;
import com.atguigu.yygh.hosp.model.hosp.Department;
import com.atguigu.yygh.hosp.model.hosp.Hospital;
import com.atguigu.yygh.hosp.model.hosp.Schedule;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.hosp.vo.hosp.DepartmentVo;
import com.atguigu.yygh.hosp.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.convert.QueryMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
@Slf4j
@Api(tags = "医院管理API接口")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //比对签名
//        String sign = (String)paramMap.get("sign");//请求中获取到的签名
//
//        String hoscode = (String)paramMap.get("hoscode");
//
//        String Sign = hospitalSetService.getSignByHoscode(hoscode);
//        String md5Sign = MD5.encrypt(Sign);//MD5加密得到的签名
//        if (!sign.equals(md5Sign) ){
//            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
//        }


        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");
//        String encrypt = MD5.encrypt(hospSign);

        //2 根据传递过来医院编码，查询数据库，查询签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignByHoscode(hoscode);

        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //4 判断签名是否一致
        if (!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        //base64转码 '+' <= ' '
        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String) paramMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        paramMap.put("logoData", logoData);

        hospitalService.save(paramMap);
        return Result.ok();
    }


    @ApiOperation(value = "获取医院信息")
    @PostMapping("hospital/show")
    public Result hospital(HttpServletRequest request) {

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //判断是否为空

        //校验signkey

        Hospital hospital = hospitalService.getByHoscode((String) paramMap.get("hoscode"));


        return Result.ok(hospital);
    }

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {

        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());

        //判断是否为空

        //校验signkey

        departmentService.save(map);

        return Result.ok();

    }

    @ApiOperation(value = "获取分页列表")
    @PostMapping("department/list")
    public Result department(HttpServletRequest request) {
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");
        //判断是否为空

        //校验signkey

        //no necessary
        int page = StringUtils.isEmpty(map.get("page")) ? 1 : Integer.parseInt((String) map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit")) ? 10 : Integer.parseInt((String) map.get("limit"));


        //build  departmentVo
        DepartmentQueryVo departmentVo = new DepartmentQueryVo();
        departmentVo.setHoscode(hoscode);
        departmentVo.setDepcode(depcode);


        //build page

        Page<Department> pageModel = departmentService.selectPage(page, limit, departmentVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {

        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");

        //params check ignore..

        departmentService.removeDepartment(hoscode,depcode);

        return Result.ok();
    }

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "删除排班")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) map.get("hoscode");
        String hosScheduleId = (String) map.get("hosScheduleId");

        //ignore check params..

        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request) {
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());

        //get page , limit
        int page = StringUtils.isEmpty(map.get("page"))?1: Integer.parseInt((String) map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit"))?10: Integer.parseInt((String) map.get("limit"));



        //create queryVo
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        Page<Schedule> pages =  scheduleService.selectPage(page,limit, scheduleQueryVo);
        return Result.ok(pages);
    }

    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {

        Map<String, Object> switchMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //params check ignore..

        scheduleService.save(switchMap);
        return Result.ok();
    }



}