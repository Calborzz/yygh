package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("用户api接口")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @PostMapping("/login")
    public R login(){
        return R.ok().data("token","admin-token");
    }

    @GetMapping("/info")
    public R info(){
        return R.ok().data("roles","[admin]")
                .data("introduction","admin")
                .data("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .data("name","admin");
    }

}
