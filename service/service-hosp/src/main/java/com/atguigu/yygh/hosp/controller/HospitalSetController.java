package com.atguigu.yygh.hosp.controller;


import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.util.MD5;
import com.atguigu.yygh.hosp.model.hosp.HospitalSet;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.vo.hosp.HospitalQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;


@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    //1 查询医院设置表所有信息
    @ApiOperation(value = "获取所有医院设置")
    @GetMapping("findAll")
    public Result findAllHospitalSet() {
        //调用service的方法

        //模拟异常
//        try {
//            int i = 1/0;
//        }catch (Exception e){
//            throw new YyghException("失败！",201);
//        }

        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);

    }


    //2 逻辑删除医院设置
    @ApiOperation(value = "逻辑删除医院设置")
    @DeleteMapping("{id}")
    public Result removeHospSet(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if (flag) {
            return Result.ok();
        }
        else {
            return Result.fail();
        }
    }

    //条件查询带分页
    @ApiOperation(value = "条件查询带分页")
    @PostMapping("findPageHospSet/{current}/{limit}")
    public Result findPageHospSet(@PathVariable long current, @PathVariable long limit,
                                  @RequestBody(required = false) HospitalQueryVo hospitalQueryVo){

        //创建一个PAGE对象
        Page<HospitalSet> page = new Page<>(current, limit);

        //构造查询条件
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(hospitalQueryVo.getHosname())){
            queryWrapper.like("hosname",hospitalQueryVo.getHosname());
        }
        if (!StringUtils.isEmpty(hospitalQueryVo.getHoscode())){
            queryWrapper.eq("hoscode",hospitalQueryVo.getHoscode());
        }

        Page<HospitalSet> page1 = hospitalSetService.page(page, queryWrapper);

        return Result.ok(page1);

    }

    //4 添加医院设置
    @ApiOperation(value = "添加医院设置")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
        //设置状态 1可以使用 0不可以使用
        hospitalSet.setStatus(1);

        //生成密钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));

        boolean save = hospitalSetService.save(hospitalSet);

        if (save) {
            return Result.ok();
        }
        else {
            return Result.fail();
        }

    }


    //5 根据id获取医院设置
    @ApiOperation(value = "根据id获取医院设置")
    @GetMapping("getHospSet/{id}")
    public Result getHospSet(@PathVariable Long id) {

        HospitalSet byId = hospitalSetService.getById(id);
        return Result.ok(byId);
    }


    //6 修改医院设置
    @ApiOperation(value = "修改医院设置")
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet) {

        boolean hospital = hospitalSetService.updateById(hospitalSet);
        if (hospital) {
            return Result.ok();
        }
        else {
            return Result.fail();
        }


    }


    //7 批量删除医院设置
    @ApiOperation(value = "批量删除医院设置")
    @DeleteMapping("batchRemove")
    public Result batchRemoveHospitalSet(@RequestBody List<Long> idList) {

        boolean removeByIds = hospitalSetService.removeByIds(idList);

        if (removeByIds) {
            return Result.ok();
        }
        else {
            return Result.fail();
        }

    }

    //8 设置医院状态
    @ApiOperation(value = "设置医院锁定状态")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable long id, @PathVariable Integer status) {

        HospitalSet hospitalSet = hospitalSetService.getById(id);

        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }


    //8 发送签名key
    @ApiOperation(value = "发送签名key")
    @PutMapping("sendKey/{id}")
    public Result sendKey(@PathVariable long id) {

        HospitalSet hospitalSet = hospitalSetService.getById(id);

        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();

        //TODO 发送短信

        return Result.ok();

    }


}
