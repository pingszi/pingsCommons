package cn.pings.jwt.verifier;

import cn.pings.commons.util.jwt.JwtUtil;
import cn.pings.jwt.exception.AccessTokenExpiredException;
import cn.pings.jwt.exception.RefreshTokenExpiredException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 *********************************************************
 ** @desc  ： refresh token和access token结合的Jwt校验器
 ** @author  Pings
 ** @date    2019/5/17
 ** @version v1.0
 * *******************************************************
 */
public class RefreshTokenJwtVerifier extends AbstractJwtVerifier {

    //**刷新信息过期时间(分钟)，默认60分钟
    protected long refreshTokenExpireTime = 60;
    //**生成签名后缓存时间(生成签名后在指定时间内不重新生成新的签名，而使用缓存)，默认5S
    private int tokenSignCacheTime = 5;
    //**redisTemplate，用于存储refresh toke，并实现多个系统之间的共享
    protected RedisTemplate<String, Object> redisTemplate;
    //**缓存中保存refreshToken key的前缀
    public static final String REFRESH_TOKEN_PREFIX = "jwt_refresh_token_";
    //**缓存中保存accessToken key的前缀
    private static final String ACCESS_TOKEN_PREFIX = "jwt_access_token_";

    public RefreshTokenJwtVerifier(RedisTemplate<String, Object> redisTemplate) {
        Assert.notNull(redisTemplate, "redisTemplate cat not be null");

        this.redisTemplate = redisTemplate;
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
    public String sign(String userName, Consumer<JWTCreator.Builder> setClaim) {
        String refreshTokenKey = this.getKey(userName);
        String accessTokenKey = ACCESS_TOKEN_PREFIX + userName;

        //**refreshToken为当前时间戳
        long refreshToken = System.currentTimeMillis();
        //**获取access token
        JWTCreator.Builder builder = JWT.create()
                .withClaim(USER_NAME, userName)
                .withClaim(REFRESH_TOKEN_PREFIX, refreshToken)
                .withExpiresAt(new Date(refreshToken + accessTokenExpireTime * 60 * 1000));
        setClaim.accept(builder);
        String accessToken = builder.sign(this.generateAlgorithm(userName));

        //**如果没有有效的accessToken，则缓存新的accessToken
        Boolean success = this.redisTemplate.opsForValue().setIfAbsent(accessTokenKey, accessToken, tokenSignCacheTime, TimeUnit.SECONDS);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RefreshTokenExpiredException("The refresh token has expired.");
        }
        //**如果缓存新的accessToken成功，则缓存新的refreshToken
        if (success != null && success) {
            this.redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, refreshTokenExpireTime, TimeUnit.MINUTES);
        } else {  //**否则，返回缓存的accessToken
            accessToken = this.redisTemplate.opsForValue().get(accessTokenKey) + "";
        }

        return accessToken;
    }

    @Override
    public boolean verify(String token) {
        String key = this.getKey(this.getUserName(token));

        //**刷新令牌不存在/过期
        Boolean hasKey = this.redisTemplate.hasKey(key);
        if (hasKey == null || !hasKey)
            throw new RefreshTokenExpiredException("The refresh token not existed or expired.");

        //**刷新令牌和访问令牌的时间戳不一致
        long refreshToken = (long) this.redisTemplate.opsForValue().get(key);
        long currentRefreshToken = this.getRefreshToken(token);
        if (refreshToken != currentRefreshToken) {
            throw new RefreshTokenExpiredException("The refresh token has expired.");
        }

        //**访问令牌校验
        try {
            return super.verify(token);
        } catch (TokenExpiredException e){
            throw new AccessTokenExpiredException("The access token has expired.");
        }
    }

    /**刷新令牌：通过删除缓存中的刷新令牌使token无效*/
    @Override
    public void invalidateSign(String userName){
        //**删除refresh token
        this.redisTemplate.delete(this.getKey(userName));
    }

    //**获取缓存中保存refreshToken的key
    private String getKey(String userName) {
        return REFRESH_TOKEN_PREFIX + userName;
    }

    //**获取jwt中的refresh token
    private long getRefreshToken(String token) {
        return JwtUtil.getValue(token, REFRESH_TOKEN_PREFIX).asLong();
    }

    public static class Builder {

        private RefreshTokenJwtVerifier verifier;

        private Builder() {
        }

        public static Builder newBuilder(RedisTemplate<String, Object> redisTemplate) {
            Builder builder = new Builder();
            builder.verifier = new RefreshTokenJwtVerifier(redisTemplate);
            return builder;
        }

        public Builder accessTokenExpireTime(long accessTokenExpireTime) {
            verifier.accessTokenExpireTime = accessTokenExpireTime;
            return this;
        }

        public Builder refreshTokenExpireTime(long refreshTokenExpireTime) {
            verifier.refreshTokenExpireTime = refreshTokenExpireTime;
            return this;
        }

        public Builder secret(String secret) {
            verifier.secret = secret;
            return this;
        }

        public Builder tokenSignCacheTime(int tokenSignCacheTime) {
            verifier.tokenSignCacheTime = tokenSignCacheTime;
            return this;
        }

        public RefreshTokenJwtVerifier build() {
            return verifier;
        }
    }
}
