package com.atguigu.yygh.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.yygh.oss.prop.OssProperties;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class OssService {
    @Autowired
    private OssProperties ossProperties;

    public String upload(MultipartFile file) {
        OSS ossClient = null;
        try {
            String endpoint = ossProperties.getEndpoint();
            String bucketName = ossProperties.getBucketName();
            String accessKeyId = ossProperties.getKeyId();
            String keySecret = ossProperties.getKeySecret();

            // 创建OSSClient实例。
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, keySecret);

            //设置文件名
            StringBuilder fileName = new StringBuilder();
            fileName.append(new DateTime().toString("yyyy/MM/dd")).append("/")
                    .append(UUID.randomUUID().toString().replace("-", ""))
                    .append(file.getOriginalFilename());

            //保存文件
            ossClient.putObject(bucketName, fileName.toString(), file.getInputStream());

            //拼写查看路径
            StringBuilder returnUrl = new StringBuilder();
            returnUrl.append("https://").append(bucketName)
                    .append(".").append(endpoint)
                    .append("/").append(fileName);
            return returnUrl.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
