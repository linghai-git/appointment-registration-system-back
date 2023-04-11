package com.atguigu.yygh.cmn.service;

import com.atguigu.yygh.hosp.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
    //根据数据id查询子数据列表
    List<Dict> findChlidData(Long id);

    /**
     * 导出
     * @param response
     */
    void exportData(HttpServletResponse response);

    //导入数据字典
    void importDictData(MultipartFile file);

    String getDictName(String DictCode, String value);

    List<Dict> findByDictCode(String dictCode);
}
