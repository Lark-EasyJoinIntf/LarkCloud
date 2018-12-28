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

    public boolean isLogin(String key){
        return true;
    }
}
