package com.atguigu.yygh.cmn.service;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.List;

public interface DictService extends IService<Dict> {

    /**
     * 根据数据id查询子数据列表
     * @param id
     * @return
     */
    List<Dict> findChildData(Long id);

    /**
     * 下载数据字典Excel文件
     * @param response
     * @throws IOException
     */
    void download(HttpServletResponse response) throws IOException;

    /**
     * 导入Excel文件
     * @param file
     * @throws IOException
     */
    void importDictData(MultipartFile file) throws IOException;

    /**
     * 删除数据字典信息
     * @param name
     * @return
     */
    R removeDict(String name);


    /**
     * 通过dictCode和value查询名字
     * @param dictCode
     * @param value
     * @return
     */
    String getNameByDictCodeAndValue(String dictCode, Long value);

    /**
     * 根据dictCode查询dict列表
     * @param dictCode
     * @return
     */
    List<Dict> findByDictCode(String dictCode);

    /**
     * 根据dictCode查询dict
     * @param dictCode
     * @return
     */
    Dict findDictByDictCode(String dictCode);

}