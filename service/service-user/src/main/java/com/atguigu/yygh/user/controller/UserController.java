package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api("后台用户管理模块")
@RestController
    @RequestMapping("/admin/userInfo")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("查询用户列表")
    @GetMapping("/{page}/{limit}")
    public R findAll(@PathVariable Long page, @PathVariable Long limit,
                     UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> userInfoPage = new Page<>(page,limit);
        IPage<UserInfo> infoIPage = userInfoService.selectPage(userInfoPage, userInfoQueryVo);
        return R.ok().data("list",infoIPage);
    }

    @ApiOperation("查看用户详细")
    @GetMapping("/show/{userId}")
    public R show(@PathVariable Long userId){
        Map<String, Object> map = userInfoService.show(userId);
        return R.ok().data(map);
    }

    @ApiOperation("锁定或取消锁定")
    @PutMapping("/lock/{id}/{status}")
    public R lock(@PathVariable Long id, @PathVariable Integer status){
        userInfoService.lock(id,status);
        return R.ok();
    }

    @ApiOperation("认证审批")
    @PutMapping("/approval/{userId}/{authStatus}")
    public R approval(@PathVariable Long userId, @PathVariable Integer authStatus){
        userInfoService.approval(userId,authStatus);
        return R.ok();
    }




}
