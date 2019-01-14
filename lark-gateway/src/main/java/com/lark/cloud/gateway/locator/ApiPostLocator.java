package com.lark.cloud.gateway.locator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lark.cloud.gateway.filter.PostGatewayFilter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * POST请求路由
 * @author xc.li
 * @create 2019-01-09
 **/
@EnableAutoConfiguration
@Configuration
@Log4j2
public class ApiPostLocator {

    @Autowired
    private PostGatewayFilter postGatewayFilter;

    @Value("${lark.routes.posts}")
    private String routes;

    /*private static final String SERVICE = "/api-a/**";
    private static final String URI = "lb://service-feign:8765";*/

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        /*
        route1 是get请求，get请求使用readBody会报错
        route2 是post请求，Content-Type是application/x-www-form-urlencoded，readbody为String.class
        route3 是post请求，Content-Type是application/json,readbody为Object.class
         */
        //RouteLocatorBuilder.Builder routes = builder.routes();
        RouteLocatorBuilder.Builder serviceProvider = null;
        JSONArray routeArr = getRoutes();
        for(int i=0; i<routeArr.size(); i++){
            if(serviceProvider == null){
                serviceProvider = builder.routes();
            }
            JSONObject route = routeArr.getJSONObject(i);
            String path = route.getString("path");
            String[] uris = route.getString("uri").split(",");
            for(int k=0; k<uris.length; k++) {
                String uri = uris[k];
                String poi = ""+i+k;
                serviceProvider = serviceProvider
                        .route("get_route" + poi,
                                r -> r.method(HttpMethod.GET).and()
                                        .path(path)
                                        .filters(f -> {
                                            f.stripPrefix(1);
                                            return f;
                                        }).uri(uri))
                        .route("post_route" + poi+ 1,
                                r -> r.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE).and()
                                        .method(HttpMethod.POST).and()
                                        .readBody(String.class, readBody -> {
                                            return true;
                                        }).and()
                                        .path(path).filters(f -> {
                                            f.stripPrefix(1).filter(postGatewayFilter)
                                            //.hystrix(config -> {config.setName("fallbackcmd").setFallbackUri("forward:/fallback");})
                                            ;
                                            return f;
                                        }).uri(uri))
                        .route("post_route" + poi + 2,
                                r -> r.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).and()
                                        .method(HttpMethod.POST).and()
                                        .readBody(Object.class, readBody -> {
                                            return true;
                                        }).and()
                                        .path(path).filters(f -> {
                                            f.stripPrefix(1).filter(postGatewayFilter)
                                            //.hystrix(config -> {config.setName("fallbackcmd").setFallbackUri("forward:/fallback");})
                                            ;
                                            return f;
                                        }).uri(uri));
            }
        }


        RouteLocator routeLocator = serviceProvider.build();
        log.info("自定义 RouteLocator is loading ... {}", routeLocator);
        return routeLocator;
    }

    private JSONArray getRoutes(){
        log.info("Lark routes config:[]"+routes);
        if(StringUtils.isNotEmpty(routes)){
            return JSONArray.parseArray(routes);
        }
        return null;
    }
}
