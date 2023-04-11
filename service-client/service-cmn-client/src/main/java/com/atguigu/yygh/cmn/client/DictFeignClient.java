package com.atguigu.yygh.cmn.client;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-cmn")
@Repository

public interface DictFeignClient {
    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/admin/cmn/dict/getName/{DictCode}/{value}")
     String getName(
            @ApiParam(name = "DictCode", value = "上级编码", required = true)
            @PathVariable("DictCode") String DictCode,

            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value);


    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/admin/cmn/dict/getName/{value}")
     String getName(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value);
}
