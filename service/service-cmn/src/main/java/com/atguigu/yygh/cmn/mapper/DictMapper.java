package com.atguigu.yygh.cmn.mapper;

import com.atguigu.yygh.hosp.model.cmn.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


//@Mapper
@Repository
public interface DictMapper extends BaseMapper<Dict> {
}
