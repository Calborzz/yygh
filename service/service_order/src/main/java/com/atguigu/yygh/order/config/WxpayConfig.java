package com.atguigu.yygh.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "weixin.pay")
@PropertySource("wxpay.properties")
@Component
@Data
public class WxpayConfig {
    private String appid;
    private String partner;
    private String partnerkey;
}
