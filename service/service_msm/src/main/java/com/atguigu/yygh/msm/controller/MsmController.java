package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Api("短信api接口")
@RestController
@RequestMapping("/user/msm")
public class MsmController {
    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @GetMapping(value = "/send/{phone}")
    public R code(@PathVariable String phone) {
        //查询redis是否存在(为了只发一次验证码能一直用，开发时使用，实际业务中不存在这条代码)
        String redisCode = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(redisCode)) return R.ok();


        //生成验证码
        String code = RandomUtil.getFourBitRandom();
        //发送短信
        boolean isSend = msmService.send(phone, code);

        if(isSend) {
            redisTemplate.opsForValue().set(phone, code,50, TimeUnit.DAYS);
            return R.ok();
        } else {
            return R.error().message("发送短信失败");
        }
    }

}
