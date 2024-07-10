package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 官方文档不建议将AnalysisEventListener交给Spring管理
 * 最好不要用@component注解
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {

    //批量处理的阈值
    private static final int num = 10;

    //外部传入的dictMapper对象
    private DictMapper dictMapper;

    //批量处理临时存储链表
    public List<Dict> dictList = new ArrayList<>();

    public DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    /**
     * 每一行读取后会调用该方法
     *
     * @param dictEeVo
     * @param analysisContext
     */
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        //将dictEeVo转换为dict
        BeanUtils.copyProperties(dictEeVo, dict);
        //先添加到临时链表
        dictList.add(dict);
        //如果链表长度达到阈值开始插入数据库
        if (dictList.size() >= num) {
            uploadFile();
        }
    }

    /**
     * 解析完最后一行执行的方法
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //将链表剩余的数据插入到数据库
        uploadFile();
    }

    /**
     * 插入数据库
     */
    private void uploadFile() {
        for (Dict d : dictList) {
            //查询是否存在该数据
            Dict dict = dictMapper.selectById(d.getId());
            System.out.println(dict);
            if (dict != null) { //不存在就添加
                dictMapper.insert(d);
            } else { //存在就更新
                dictMapper.updateById(d);
            }
        }
    }

}
