package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.netflix.client.http.HttpRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api("用户api接口")
@RestController
@RequestMapping("/user/userInfo")
public class UserInfoController{

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("用户登录或注册功能")
    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo) {
        Map<String, Object> info = userInfoService.login(loginVo);
        return R.ok().data(info);
    }

    @ApiOperation("用户实名认证")
    @PostMapping("/auth/userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, @RequestHeader String token){
        userInfoService.userAuth(JwtHelper.getUserId(token),userAuthVo);
        return R.ok();
    }

    @ApiOperation("获取用户id信息接口")
    @GetMapping("/auth/getUserInfo")
    public R getUserInfo(@RequestHeader String token) {
        Long userId = JwtHelper.getUserId(token);
        UserInfo userInfo = userInfoService.getByUserInfo(userId);
        return R.ok().data("userInfo",userInfo);
    }
}
