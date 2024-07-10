package com.atguigu.yygh.gateway.filter;

import com.google.gson.JsonObject;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 * 全局Filter，统一处理会员登录与外部不允许访问的服务
 * </p>
 */
//@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        //api接口，校验必须登录
        //如果是进入用户服务模块就放行
        if(antPathMatcher.match("/admin/user/**", path)) {
            return chain.filter(exchange);
        }else { //其他接口
            List<String> tokenList = request.getHeaders().get("X-Token");
            //如果没有token就跳转到登入页面
            if(tokenList == null) {  //没有token请求头
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //路由跳转
                response.getHeaders().set(HttpHeaders.LOCATION,"http://localhost:9528");
                return response.setComplete(); //结束请求
            } else {  //有token请求头
                return chain.filter(exchange); //放行
            }

        }

    }

    @Override
    public int getOrder() { //值越低越先执行
        return 0;
    }

    private Mono<Void> out(ServerHttpResponse response) {  //拒绝方法
        JsonObject message = new JsonObject();
        message.addProperty("success", false);
        message.addProperty("code", 28004);
        message.addProperty("data", "鉴权失败");
        byte[] bits = message.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //response.setStatusCode(HttpStatus.UNAUTHORIZED);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }
}
