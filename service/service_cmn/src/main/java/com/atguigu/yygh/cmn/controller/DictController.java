package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Api("数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
public class DictController {
    @Autowired
    private DictService dictService;

    @ApiOperation("根据数据id查询子数据列表")
    @GetMapping("/dictList/{id}")
    public R dictList(@PathVariable Long id){
        List<Dict> list = dictService.findChildData(id);
        return R.ok().data("list",list);
    }

    @ApiOperation("下载数据字典Excel表格")
    @GetMapping("/exportData")
    public void exportData(HttpServletResponse response) throws IOException {
        dictService.download(response);
    }

    @ApiOperation("导入Excel表格到数据字典")
    @PostMapping("/importData") //使用MultipartFile类型进行接收,名字必须与前端保存一致(file)
    public R importData(MultipartFile file) throws IOException {
        dictService.importDictData(file);
        return R.ok();
    }

    @ApiOperation("删除指定字典")
    @DeleteMapping("/deleteData/{dictName}")
    public R deleteData(@PathVariable String dictName) throws IOException {
        return dictService.removeDict(dictName);
    }


    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{dictCode}/{value}")
    public String getName(@PathVariable("dictCode") String parentDictCode,
                            @PathVariable("value") Long value) {
        return dictService.getNameByDictCodeAndValue(parentDictCode, value);
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getName(@PathVariable("value") Long value) {
        return dictService.getNameByDictCodeAndValue("", value);
    }

    @ApiOperation("根据dictCode获取下级节点")
    @GetMapping("/findByDictCode/{dictCode}")
    public R findByDictCode(@PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list",list);
    }

    @ApiOperation("根据dictCode获取下级节点")
    @GetMapping("/findChildData/{id}")
    public R findChildData(@PathVariable Long id) {
        //查询传入的字典编码
        Dict dict = dictService.getById(id);
        //查询该节点的子节点数据
        List<Dict> list = dictService.findChildData(dict.getId());
        return R.ok().data("list",list);
    }
}
