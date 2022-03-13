package com.max.gmall0822.util;

import io.jsonwebtoken.*;

import java.util.Map;

public class JwtUtil {

    //編碼
    /*
    key : 加解密簽名用, 真正需要保密的部分就是簽名部分
    param : 私有部分
    salt : 鹽值, 存運行環境(ip地址...), 以防有人竊取你的票,驗票還要驗出身地.
     */
    public static String encode(String key,Map<String,Object> param,String salt){
        if(salt!=null){
            salt="0"; //ip地址一直取不好,嘗試暴力法
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }

    //解碼
    public  static Map<String,Object> decode(String token , String key, String salt){
        Claims claims=null;
        if (salt!=null){
            salt="0"; //ip地址一直取不好,嘗試暴力法
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
            return null;
        }
        return  claims;
    }

}
