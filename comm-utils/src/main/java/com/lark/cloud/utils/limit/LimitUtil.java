package com.lark.cloud.utils.limit;

import com.alibaba.fastjson.JSONObject;
import com.lark.cloud.utils.AppPropUtil;
import com.lark.cloud.utils.base.entity.Status;
import com.lark.cloud.utils.jwt.JWTTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 权限相关操作工具类
 * @date 2018-12
 * @author xc.li
 */
@Component
public class LimitUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 该方法需要调用权限服务提供的token验证接口
     * @param key 值为登录信息的 手机号码:随机值
     * @return
     */
    public boolean isLogin(String key, String token){
        String val = stringRedisTemplate.opsForValue().get(key);
        if(token.equals(val))
            return true;
        else
            return false;
    }

    /**
     * token参数名定义
     */
    final public static String TOKEN_KEY = "token";
    /**
     * 自定义的登录sessionid名称
     */
    private static String ACCOUNT_KEY = AppPropUtil.get("lark.user.attr.keyname");
    /**
     * token的有效时间（建议与redis的都保持一致）
     */
    private static String EXPIRATION_TIME = AppPropUtil.get("lark.user.expiration.time");

    /**
     * 设置登录信息到redis并jwt加密返回
     * @param account 手机号或唯一性值
     * @param user 用户信息
     * @return
     */
    public String setLoginInfo(String account, JSONObject user){
        //删除之前登录的同一用户的登录session
        Set<String> keys = stringRedisTemplate.keys(account + ":*");
        stringRedisTemplate.delete(keys);
        //生成sessionid
        String sessionid = account+":"+ UUID.randomUUID().toString();
        user.put(ACCOUNT_KEY, sessionid);
        long ttlMillis = StringUtils.isEmpty(EXPIRATION_TIME) ? 0 : Long.parseLong(EXPIRATION_TIME);
        //生成token并缓存到redis
        String jwt = JWTTokenUtil.getInstance().createJWTToken(user.toJSONString(), ttlMillis);
        stringRedisTemplate.opsForValue().set(sessionid, jwt, ttlMillis);
        return jwt;
    }

    public static boolean checkUri(List<String> urlList, String uri){
        if(urlList.contains(uri)){
            return true;
        }
        boolean pass = false;
        StringBuffer ub = new StringBuffer(128);
        ub.append("/");
        String[] uriArr = uri.split("/");
        int i = 0;
        for(String ur : uriArr){
            if(i < 2){//从第3个值开始
                i++;
                continue;
            }

            ub.append(ur);
            //包含 /xxx或/xxx/*都放行
            if(urlList.contains(ub.toString()) || urlList.contains(ub.toString()+"/*")){
                pass = true;
                break;
            }
        }
        return pass;
    }

    public Status checkToken(String token){
        Status status = Status.SUCCESS;
        if (StringUtils.isNotEmpty(token)) {
            JSONObject user = JWTTokenUtil.getInstance().getUserInfo(token);
            if (user == null) {
                status = Status.LIMIT_USER_NOT_LOGIN;
            } else if (!user.containsKey(ACCOUNT_KEY) || StringUtils.isEmpty(user.getString(ACCOUNT_KEY))) {
                status = Status.LIMIT_USER_LOST_ACCOUNT_ATTR;
            } else if (!isLogin(user.getString(ACCOUNT_KEY), token)) {//ACCOUNT_KEY的值为登录信息的 手机号码:随机值
                status = Status.LIMIT_USER_INVALID;
            }
        } else {
            status = Status.LIMIT_TOKEN_MISS;
        }
        return status;
    }
}
