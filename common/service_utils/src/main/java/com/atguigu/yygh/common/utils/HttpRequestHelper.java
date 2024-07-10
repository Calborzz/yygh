package com.atguigu.yygh.common.utils;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestHelper {
    /**
     * 将map类型转化
     * @param map
     * @return
     */
    public static Map<String,Object> switchMap(Map<String,String[]> map){
        HashMap<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            resultMap.put(entry.getKey(),entry.getValue()[0]);
        }
        return resultMap;
    }


}
