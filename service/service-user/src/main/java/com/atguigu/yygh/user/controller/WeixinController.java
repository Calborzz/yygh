package com.atguigu.yygh.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信登录接口文档
 * https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=e547653f995d8f402704d5cb2945177dc8aa4e7e&lang=zh_CN
 */
@Api("微信api接口")
@Controller
@RequestMapping("/user/userInfo/weixin")
public class WeixinController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate redisTemplate;


    @ApiOperation("获取微信登录参数")
    @GetMapping("/getLoginParam")
    @ResponseBody
    public R genQrConnect() throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis() + "");
        return R.ok().data(map);
    }

    @ApiOperation("微信服务回调地址")
    @GetMapping("/callback")
    public String callback(String code, String state) {
        try {
            //获取accessToken值和openid值的请求地址
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
            String format = String.format(url, ConstantPropertiesUtil.WX_OPEN_APP_ID, ConstantPropertiesUtil.WX_OPEN_APP_SECRET, code);
            //再用返回的code参数请求微信
            String json = HttpClientUtils.get(format);
            JSONObject jsonObject = JSONObject.parseObject(json);
            String accessToken = jsonObject.getString("access_token"); //临时接口调用凭证
            String openid = jsonObject.getString("openid"); //授权用户唯一标识
            //查询用户
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("openid", openid);
            UserInfo userInfo = userInfoService.getOne(wrapper);

            //注册
            if (userInfo == null) {  //不存在代表是第一次登录
                //通过accessToken值和openid值访问用户具体信息
                String url1 = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
                String format1 = String.format(url1, accessToken, openid);
                //获取用户信息
                String json1 = HttpClientUtils.get(format1);
                JSONObject jsonObject1 = JSONObject.parseObject(json1);
                String nickname = jsonObject1.getString("nickname");//获取用户名称
                String headimgurl = jsonObject1.getString("headimgurl");
                //封装用户信息
                userInfo = new UserInfo();
                userInfo.setOpenid(openid);
                userInfo.setNickName(nickname);
                userInfo.setStatus(1);
                //保存到数据库
                userInfoService.save(userInfo);
            }
            //登录
            HashMap<String, String> map = new HashMap<>();
            String name = userInfo.getName();
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            if (StringUtils.isEmpty(name)) {
                name = "新用户";
            }
            //判断userInfo是否有手机号，如果手机号为空，返回openid
            //如果手机号不为空，返回openid值是空字符串
            //前端判断：如果openid不为空，绑定手机号，如果openid为空，不需要绑定手机号
            if (StringUtils.isEmpty(userInfo.getPhone())) {
                map.put("openid", userInfo.getOpenid());
            } else {
                map.put("openid", "");
            }
            //生成token
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("name", name);
            map.put("token", token);
            String returnUrl = "redirect:http://localhost:3000/weixin/callback?token=%s&openid=%s&name=%s";
            String result = String.format(returnUrl, token, map.get("openid"), URLEncoder.encode(name,"utf-8"));
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}