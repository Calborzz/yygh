package com.atguigu.yygh.msm.service.impl;


import com.atguigu.yygh.msm.service.MsmService;

import com.atguigu.yygh.msm.utils.HttpUtils;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class MsmServiceImpl implements MsmService {

    /**
     * 发送短信验证码API
     */
    @Override
    public boolean send(String PhoneNumbers, String templateCode) {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "b5d93ae21ef54a8a932519cd485d84ff";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", PhoneNumbers);
        querys.put("param", "**code**:"+templateCode+",**minute**:5");
        //smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());

            return true;
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送短信接口
     * @param msmVo
     * @return
     */
    @Override
    public boolean send(MsmVo msmVo) {
        if(!StringUtils.isEmpty(msmVo.getPhone())){
            //因为不能发送模板信息了  只能先模拟了
            System.out.println("msmVo = " + msmVo);
        }
        return true;
    }

}