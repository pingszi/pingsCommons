package cn.pings.jwt.verifier;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 *********************************************************
 ** @desc  ： 基于access token的Jwt校验器
 ** @author  Pings
 ** @date    2019/5/17
 ** @version v1.0
 * *******************************************************
 */
public class AccessTokenJwtVerifier extends AbstractJwtVerifier {

    //**访问令牌过期时间(分钟)，默认600分钟，基于access token的方式，过期时间设置比较长，防止重复登录
    protected long accessTokenExpireTime = 600;

    public AccessTokenJwtVerifier(){
    }

    public AccessTokenJwtVerifier(String secret, long accessTokenExpireTime){
        if(StringUtils.isBlank(secret) || accessTokenExpireTime <= 0)
            throw new IllegalArgumentException("secret and accessTokenExpireTime catnot be null");

        this.secret = secret;
        this.accessTokenExpireTime = accessTokenExpireTime;
    }

    @Override
    public String sign(String userName) {
        return sign(userName, new HashMap<>());
    }

    @Override
    public String sign(String userName, Map<String, String> params){
        return sign(userName, builder -> params.forEach(builder::withClaim));
    }

    @Override
    public String sign(String userName, Consumer<JWTCreator.Builder> setClaim){
        JWTCreator.Builder builder = JWT.create().withClaim(USER_NAME, userName).withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpireTime * 60 * 1000));
        setClaim.accept(builder);

        return builder.sign(this.generateAlgorithm(userName));
    }

}
