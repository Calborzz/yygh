package com.atguigu.yygh.oss.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.oss.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api("阿里云文件管理")
@RestController
@RequestMapping("/admin/oss/file")
public class OssController {

    @Autowired
    private OssService ossService;

    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public R upload(MultipartFile file) {
        String uploadUrl = ossService.upload(file);
        if (StringUtils.isEmpty(uploadUrl)){
            return R.error().message("文件上传失败").data("url", uploadUrl);
        }else {
            return R.ok().message("文件上传成功").data("url", uploadUrl);
        }
    }
}
