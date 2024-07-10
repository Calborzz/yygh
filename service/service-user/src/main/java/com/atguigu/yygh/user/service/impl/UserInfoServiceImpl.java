package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1.获取参数
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        String openid = loginVo.getOpenid();
        //2.参数验空
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(20001, "数据为空");
        }
        //3.校验验证码
        String mobleCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(mobleCode)) {
            throw new YyghException(20001, "验证码失败");
        }
        //4.查询手机号的用户
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        //5.手机号没有就注册
        if (userInfo == null) {

            if (!StringUtils.isEmpty(openid)) {  //有微信号 没手机号
                //这是先微信登录再绑定手机号
                QueryWrapper<UserInfo> openidWrapper = new QueryWrapper<>();
                openidWrapper.eq("openid",openid);
                UserInfo userByOpenid = baseMapper.selectOne(openidWrapper);
                if (userByOpenid==null){
                    throw new YyghException(20001,"openid无效");
                }
                userByOpenid.setPhone(phone);
                userInfo = userByOpenid;
                this.update(userByOpenid,openidWrapper); //更新
            }else { //没有微信号 直接注册手机号
                userInfo = new UserInfo();
                userInfo.setCreateTime(new Date());
                userInfo.setUpdateTime(new Date());
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                this.save(userInfo); //新增
            }
        //手机号已注册
        }else {
            //先注册了手机号再绑定微信
            if (!StringUtils.isEmpty(openid)) { //openid不为空
                QueryWrapper<UserInfo> openidWrapper = new QueryWrapper<>();
                openidWrapper.eq("openid",openid);
                UserInfo userByOpenid = baseMapper.selectOne(openidWrapper);
                if (userByOpenid==null){
                    throw new YyghException(20001,"openid无效");
                }
                //把openid和nickname添加进去
                userInfo.setOpenid(userByOpenid.getOpenid());
                userInfo.setNickName(userByOpenid.getNickName());
                this.remove(openidWrapper); //删除前面注册的微信号
                this.update(userInfo,wrapper);
            }
        }

        //6.登录(注册后也进行登录)
        if (userInfo.getStatus() == 0) {
            throw new YyghException(20001, "用户已经禁用");
        }
        //7.返回名称
        HashMap<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) { //没name就返回NickName
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) { //没nickName就返回phone
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //8.jwt生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }

    @Override
    public UserInfo getByUserInfo(Long userId) {
        UserInfo userInfo = baseMapper.selectById(userId);
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        return userInfo;
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //获取数据
        String keyword = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        //非空判断
        if(!StringUtils.isEmpty(keyword)) {
            wrapper.like("name",keyword).or().like("phone",keyword);
        }
        if(!StringUtils.isEmpty(status)) {
            wrapper.eq("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageParam, wrapper);
        //循环封装数据
        userInfoPage.getRecords().stream().forEach(this::packageUserInfo);
        return userInfoPage;
    }

    @Override
    public void lock(Long id, Integer status) {
        if(status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = this.getById(id);
            userInfo.setStatus(status.intValue());
            this.updateById(userInfo);
        }
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //查询用户
        UserInfo userInfo = baseMapper.selectById(userId);
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        baseMapper.updateById(userInfo);
    }
    @Override
    public Map<String, Object> show(Long userId) {
        HashMap<String, Object> map = new HashMap<>();
        UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(userId));
        map.put("userInfo",userInfo);
        List<Patient> patientList = patientService.findAllUserId(userId);
        map.put("patientList",patientList);
        return map;
    }

    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus.intValue()==-1||authStatus.intValue()==2){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    //编号变成对应值封装
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue()==0 ?"锁定" : "正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }
}
