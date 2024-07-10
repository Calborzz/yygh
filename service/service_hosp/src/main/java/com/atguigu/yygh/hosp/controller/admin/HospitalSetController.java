package com.atguigu.yygh.hosp.controller.admin;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2024-05-25
 */
@Api("医院api接口")
@RestController
@RequestMapping("/admin/hosp/hospital-set")
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("查询所有医院设置列表")
    @GetMapping("/findAll")
    public R findAll() {
        return R.ok().data("list", hospitalSetService.list());
    }


    @ApiOperation("条件分页查询医院设置列表")
    @PostMapping("/page/{page}/{size}")
    public R pageList(@PathVariable Long page, @PathVariable Long size,
                      @RequestBody HospitalSetQueryVo hospitalSetQueryVo){
        //获取请求参数并添加查询条件
        Page<HospitalSet> pageList = new Page<>(page,size);
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        if (hospitalSetQueryVo == null){
            //进行查询条件分页信息
            hospitalSetService.page(pageList,wrapper);
        }else {
            if (!StringUtils.isEmpty(hospitalSetQueryVo.getHosname())){
                wrapper.like("hosname",hospitalSetQueryVo.getHosname());
            }
            if (!StringUtils.isEmpty(hospitalSetQueryVo.getHoscode())){
                wrapper.eq("hoscode",hospitalSetQueryVo.getHoscode());
            }
            //进行查询条件分页信息
            hospitalSetService.page(pageList,wrapper);
        }
        //获取数据
        List<HospitalSet> records = pageList.getRecords();
        //获取总页数
        long total = pageList.getTotal();
        return R.ok().data("total",total).data("rows",records);
    }



    @ApiOperation("新增医院设置")
    @PostMapping("/saveHospSet")
    public R save(@RequestBody HospitalSet hospitalSet) {
        //生成随机数加密并保存
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + new Random().nextInt(1000)));
        hospitalSetService.save(hospitalSet);
        return R.ok();
    }

    @ApiOperation("根据id查询医院设置信息")
    @GetMapping("/getHospSetById/{id}")
    public R getHospSetById(@PathVariable Long id) {
        HospitalSet byId = hospitalSetService.getById(id);
        return R.ok().data("item", byId);
    }

    @ApiOperation("按照id删除医院设置")
    @DeleteMapping("/deleteHospSetById/{id}")
    public R deleteById(@PathVariable Long id) {
        hospitalSetService.removeById(id);
        return R.ok();
    }

    @ApiOperation("根据id修改医院设置信息")
    @PostMapping("/updateHospSet")
    public R updateById(@RequestBody HospitalSet hospitalSet) {
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    @ApiOperation("批量删除医院设置信息")
    @DeleteMapping("/batchRemove")
    public R batchRemoveHospSet(@RequestBody List<Long> list) {
        hospitalSetService.removeByIds(list);
        return R.ok();
    }

    @ApiOperation("医院设置锁定和解锁")
    @PutMapping("/lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable Long id, @PathVariable Integer status) {
        //设置状态
        HospitalSet hospitalSet = new HospitalSet();
        hospitalSet.setId(id);
        hospitalSet.setStatus(status);
        //调用服务
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

}

