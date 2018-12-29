package com.lark.cloud.utils.limit;

public class TokenValidUtil {
    private static TokenValidUtil instance;

    private TokenValidUtil(){}

    public static TokenValidUtil getInstance(){
        if(instance == null){
            instance = new TokenValidUtil();
        }
        return instance;
    }

    /**
     * 该方法需要调用权限服务提供的token验证接口
     * @param key
     * @return
     */
    public boolean isLogin(String key, String token){
        return true;
    }
}
