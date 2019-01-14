package com.lark.cloud.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.lark.cloud.utils.base.entity.Result;
import com.lark.cloud.utils.base.entity.Status;
import com.lark.cloud.utils.limit.LimitUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * GET请求过滤器
 * 权限验证，放行路径/vip/* -用于用户登录注册
 * sessionid的值为登录信息的 手机号码:随机值
 * @author xc.li
 * @create 2019-01-09
 **/
@Log4j2
@Component
public class GetFilter implements GlobalFilter, Ordered {

    @Autowired
    private LimitUtil limitUtil;

    List<String> noFilterList = new ArrayList<String>(){{
        add("/vip/*");
        add("/bullet/*");
    }};
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uri = exchange.getRequest().getURI().getPath();
        log.info("GET请求地址:", uri);
        if(uri != null && LimitUtil.checkUri(noFilterList, uri)){
            return chain.filter(exchange);
        }
        Result result = new Result();
        Status status = Status.SUCCESS;
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        try {
            HttpMethod method = request.getMethod();
            if(HttpMethod.GET.equals(method)){
                String token = request.getQueryParams().getFirst(LimitUtil.TOKEN_KEY);
                if(StringUtils.isEmpty(token))
                    token = request.getHeaders().getFirst(LimitUtil.TOKEN_KEY);
                log.info("请求体:{}", token);
                status = limitUtil.checkToken(token);
                if(status.getStatus() >= 0) {//成功
                    return chain.filter(exchange);
                }else{
                    response.setStatusCode( HttpStatus.UNAUTHORIZED );
                }
            } else {
                return chain.filter(exchange);
            }
        }catch (Exception e){
            response.setStatusCode( HttpStatus.EXPECTATION_FAILED );
            status = Status.FAILED;
        }
        result.setStatus(status);
        byte[] bytes = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
