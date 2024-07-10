package com.atguigu.yygh.msm.service;


import com.atguigu.yygh.vo.msm.MsmVo;

public interface MsmService {

    /**
     * 发送短信验证码API
     * @param PhoneNumbers
     * @param templateCode
     * @return
     */
    boolean send(String PhoneNumbers, String templateCode);



    /**
     * 发送短信接口
     * @param msmVo
     * @return
     */
    boolean send(MsmVo msmVo);
}
