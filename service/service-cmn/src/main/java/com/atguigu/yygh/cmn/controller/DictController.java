package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


@Api(description = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
@Slf4j
public class DictController {

    @Autowired
    private DictService dictService;

    @ApiOperation(value = "显示所有数据")
    @GetMapping("/findAll")
    public Result findAll(){
//        List<String> strings = new ArrayList<>();
//        strings.add("hello");
//        strings.add("world");

        List<Dict> list = dictService.list();
        System.out.println(list.toString());
        return  Result.ok(list);
    }
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public Result<List<Dict>> findByDictCode(
            @ApiParam(name = "dictCode", value = "节点编码", required = true)
            @PathVariable String dictCode) {
        log.info("this is dict controller！");
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }

        //根据数据id查询子数据列表
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("/findChildData/{id}")
    public Result findChildData(@PathVariable Long id) {
        log.info("this is dict controller！");
        log.debug("this is dict controller！");
        List<Dict> list = dictService.findChlidData(id);
        return Result.ok(list);
    }

    @ApiOperation(value="导出")
    @GetMapping(value = "/exportData")
    public void exportData(HttpServletResponse response) {
        dictService.exportData(response);
    }

    @ApiOperation(value = "导入")
    @CacheEvict(value = "dict", allEntries=true)//allEntries = true: 方法调用后清空所有缓存
    @PostMapping("importData")
    public Result importData(MultipartFile file) {
        dictService.importDictData(file);
        return Result.ok();
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{DictCode}/{value}")
    public String getName(
            @ApiParam(name = "DictCode", value = "上级编码", required = true)
            @PathVariable("DictCode") String DictCode,

            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {


        String dictName = dictService.getDictName(DictCode, value);
        return dictName;
    }


    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {

        String dictName = dictService.getDictName("",value);
        return dictName;
    }

    }
