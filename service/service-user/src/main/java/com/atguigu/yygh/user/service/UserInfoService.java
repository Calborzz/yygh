package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    //会员登录
    Map<String, Object> login(LoginVo loginVo);
    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    UserInfo getByUserInfo(Long userId);

    //用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    void lock(Long id, Integer status);

    //用户详细
    Map<String, Object> show(Long userId);

    /**
     * 认证审批
     * @param userId
     * @param authStatus 2：通过 -1：不通过
     */
    void approval(Long userId, Integer authStatus);
}