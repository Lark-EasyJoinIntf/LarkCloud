package com.lark.cloud.utils.limit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TokenValidUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 该方法需要调用权限服务提供的token验证接口
     * @param key
     * @return
     */
    public boolean isLogin(String key, String token){
        Object val = redisTemplate.opsForValue().get(key);
        if(token.equals(val))
            return true;
        else
            return false;
    }
}
