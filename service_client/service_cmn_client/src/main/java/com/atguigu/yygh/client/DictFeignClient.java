package com.atguigu.yygh.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 数据字典API接口
 */
@FeignClient("service-cmn")
public interface DictFeignClient {

    /**
     * 获取数据字典名称
     * @param dictCode
     * @param value
     * @return
     */
    @GetMapping(value = "/admin/cmn/dict/getName/{dictCode}/{value}")
    String getName(@PathVariable("dictCode") String dictCode, @PathVariable("value") Long value);

    /**
     * 获取数据字典名称
     * @param value
     * @return
     */
    @GetMapping(value = "/admin/cmn/dict/getName/{value}")
    String getName(@PathVariable("value") Long value);

}