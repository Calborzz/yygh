package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /**
     * 根据数据id查询子数据列表
     */
    @Override
    @Cacheable(value = "dict",key = "'selectIndexList'+#id")
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        //查找把该id作为父节点的数据（子节点数据）
        List<Dict> dictList = baseMapper.selectList(wrapper);
        //向子节点集合每个dict对象中设置hasChildren
        for (Dict dict : dictList) {
            //通过id判断是否有子节点
            dict.setHasChildren(this.isChildren(dict.getId()));
        }
        return dictList;
    }

    /**
     * 判断id下面是否有子节点
     */
    private boolean isChildren(Long dictId) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", dictId);
        //查询把该id作为父节点的数量
        Integer count = baseMapper.selectCount(wrapper);
        //大于0 = 有子节点
        return count > 0;
    }

    /**
     * 下载数据字典Excel表格
     *
     * @param response
     */
    @Override
    public void download(HttpServletResponse response) throws IOException {
        //添加响应头信息
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("数据字典", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        //将查到的dict数据转换为dictEeVos
        List<Dict> dictList = baseMapper.selectList(null);
        List<DictEeVo> dictEeVos = new ArrayList<>();
        for (Dict dict : dictList) {
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict, dictEeVo);
            dictEeVos.add(dictEeVo);
        }
        //将数据写给用户   使用response.getOutputStream()
        EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictEeVos);
    }

    /**
     * 导入数据到字典
     *
     * @param file
     */
    @Override
    @CacheEvict(value = "dict",allEntries = true)
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件路径错误");
        }
    }

    /**
     * 删除字典数据(可删除两个层级的数据)
     *
     * @param name
     * @return
     */
    @Override
    public R removeDict(String name) {
        //查询该字典数据
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        Dict dict = baseMapper.selectOne(wrapper);
        //不存在就返回false
        if (dict == null) {
            return R.error().message("不存在该数据");
        }
        //存在就查询是否有子节点
        QueryWrapper<Dict> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("parent_id", dict.getId());
        List<Dict> dictList = baseMapper.selectList(wrapper1);
        //如果有子节点
        if (dictList.size() > 0) {
            List<Long> list = new ArrayList<>();
            list.add(dict.getId());
            //判断子节点是否还有子节点
            for (Dict d : dictList) {
                //如果子节点又存在子节点则删除失败
                if (isChildren(d.getId())) {
                    list.clear();
                    return R.error().message("不可删除两个以上层级的数据");
                }
                list.add(d.getId());
            }
            //如果子节点没有孩子就执行删除操作
            baseMapper.deleteBatchIds(list);
            return R.ok().message("删除成功");
        } else {
            //没有子节点
            baseMapper.deleteById(dict.getId());
            return R.ok().message("删除成功");
        }

    }


    @Override
    public String getNameByDictCodeAndValue(String dictCode, Long value) {
        //查询只有value值的
        if (StringUtils.isEmpty(dictCode)){
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value",value);
            Dict dict = baseMapper.selectOne(wrapper);
            if (dict!=null){
                return dict.getName();
            }
        //查询dictCode和value值的
        }else {
            //第一次通过dictCode查出parentId
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("dict_code",dictCode);
            Dict dict = baseMapper.selectOne(wrapper);
            if (dict==null) return "";
            //第二次通过parentId和value查出名称
            QueryWrapper<Dict> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("parent_id",dict.getId());
            wrapper1.eq("value",value);
            Dict result = baseMapper.selectOne(wrapper1);
            if (result!=null){
                return result.getName();
            }
        }
        return "";
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict codeDict = this.findDictByDictCode(dictCode);
        if(null == codeDict) return null;
        return this.findChildData(codeDict.getId());
    }

    @Override
    public Dict findDictByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        return baseMapper.selectOne(wrapper);
    }

}
