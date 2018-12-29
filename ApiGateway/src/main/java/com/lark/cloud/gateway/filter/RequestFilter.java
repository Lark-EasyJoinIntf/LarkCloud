package com.lark.cloud.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.lark.cloud.utils.AppPropUtil;
import com.lark.cloud.utils.base.entity.Result;
import com.lark.cloud.utils.base.entity.Status;
import com.lark.cloud.utils.jwt.JWTTokenUtil;
import com.lark.cloud.utils.limit.TokenValidUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.micrometer.core.instrument.util.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 网关请求权限过滤器，放行路径/vip/*，用于用户登录注册
 */
@Component
public class RequestFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(RequestFilter.class);

    /**
     * filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下：
     pre：路由之前
     routing：路由之时
     post： 路由之后
     error：发送错误调用
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * filterOrder：过滤的顺序
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    //初始化Map
    /*private static Map<String, String > noFilter = new HashMap<String, String>(){{
        put("a","b");
        put("a","b");
    }};*/
    /*List<String> noFilterList = new ArrayList<String>(){{
        add("/login/*");
    }};*/

    private String noFilterList = "/vip/*";
    /**
     * shouldFilter：这里可以写逻辑判断，是否要过滤，本文true,永远过滤。
     * @return
     */
    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        String uri = request.getRequestURI();
        if(uri != null && checkUri(noFilterList, uri)){
            return false;
        }
        return true;
    }
    private boolean checkUri(String urlList, String uri){
        if(urlList.contains(uri)){
            return true;
        }
        boolean pass = false;
        StringBuffer ub = new StringBuffer(64);
        String[] uriArr = uri.split("/");
        for(String ur : uriArr){
            if("".equals(ur.trim()))
                continue;
            ub.setLength(0);
            ub.append("/").append(ur).append("/");
            if(urlList.contains(ub.toString()) || urlList.contains(ub.toString()+"*")){
                pass = true;
                break;
            }
        }
        return pass;
    }

    private boolean checkUri(List<String> urlList, String uri){
        if(urlList.contains(uri)){
            return true;
        }
        boolean pass = false;
        StringBuffer ub = new StringBuffer(128);
        ub.append("/");
        String[] uriArr = uri.split("/");
        for(String ur : uriArr){
            if("".equals(ur.trim()))
                continue;
            ub.append(ur).append("/");
            if(urlList.contains(ub.toString()+"*")){
                pass = true;
                break;
            }
        }
        return pass;
    }

    private static String TOKEN_KEY = "token";
    private static String ACCOUNT_KEY = AppPropUtil.get("lark.user.attr.account");
    /**
     * run：过滤器的具体逻辑。可用很复杂，包括查sql，nosql去判断该请求到底有没有权限访问。
     * @return
     */
    @Override
    public Object run() {
        Result result = new Result();
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        System.out.println("请求地址："+request.getRequestURI());
        String token = null;
        Status status = Status.SUCCESS;
        try {
            if (hasBody(request.getMethod())) {
                if (!ctx.isChunkedRequestBody()) {
                    JSONObject params = getBody(request);
                    if (params.containsKey(TOKEN_KEY)) {
                        token = params.getString(TOKEN_KEY);
                    } else {
                        status = Status.LIMIT_TOKEN_MISS;
                    }
                }
            } else {
                token = request.getParameter(TOKEN_KEY);
            }
            //解码token，验证是否登录及存在
            if (StringUtils.isNotEmpty(token)) {
                JSONObject user = JWTTokenUtil.getInstance().getUserInfo(token);
                if(user == null){
                    status = Status.LIMIT_USER_NOT_LOGIN;
                }else if(!user.containsKey(ACCOUNT_KEY) || StringUtils.isEmpty(user.getString(ACCOUNT_KEY))){
                    status = Status.LIMIT_USER_LOST_ACCOUNT_ATTR;
                }else if(!TokenValidUtil.getInstance().isLogin(user.getString(ACCOUNT_KEY))){
                    status = Status.LIMIT_USER_INVALID;
                }
            } else {
                status = Status.LIMIT_TOKEN_MISS;
            }
            if(status.getStatus() < 0) {
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(401);
                result.setStatus(status);
                ctx.setResponseBody(JSONObject.toJSONString(result));
                ctx.getResponse().setContentType("text/html;charset=UTF-8");
            }
        }catch (Exception e){
            log.error("gateway err:", e);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            status = Status.EXC;
            status.setMessage(e.getMessage());
            result.setStatus(status);
            ctx.setResponseBody(JSONObject.toJSONString(result));
        }
        return null;
    }

    private JSONObject getBody(HttpServletRequest request) throws IOException {
        ServletInputStream inp = request.getInputStream();
        String body = null;
        if (inp != null) {
            body = IOUtils.toString(inp);
        }
        if(StringUtils.isNotEmpty(body)){
            return JSONObject.parseObject(body);
        }else{
            return new JSONObject();
        }
    }

    private boolean hasBody(String method) {
        //只记录这3种谓词的body
        if ("POST".equals(method) || "PUT".equals(method))
            return true;
        return false;
    }

}
