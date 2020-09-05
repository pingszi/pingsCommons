package cn.pings.jwt.verifier;

import cn.pings.commons.util.jwt.JwtUtil;
import cn.pings.jwt.exception.AccessTokenExpiredException;
import cn.pings.jwt.exception.RefreshTokenExpiredException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.util.*;
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

    //**刷新令牌过期时间(分钟)，默认60分钟
    protected long refreshTokenExpireTime = 60;
    //**刷新令牌过期延迟时间(刷新令牌过期，但是过期时间小于延迟时间，则尝试重新登录)，默认10S
    private int tokenSignCacheTime = 50;
    //**redisTemplate，用于存储refresh toke，并实现多个系统之间的共享
    protected RedisTemplate<String, Object> redisTemplate;
    //**缓存中保存refreshToken key的前缀
    public static final String REFRESH_TOKEN_PREFIX = "jwt_refresh_token_";

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
        return sign(userName, setClaim, null);
    }

    @Override
    public String sign(String userName, Consumer<JWTCreator.Builder> setClaim, String tokenMd5) {
        String refreshTokenKey = this.getKey(userName);

        //**refreshToken为当前时间戳
        long refreshToken = System.currentTimeMillis();
        //**获取access token
        JWTCreator.Builder builder = JWT.create()
                .withClaim(USER_NAME, userName)
                .withClaim(REFRESH_TOKEN_PREFIX, refreshToken)
                .withExpiresAt(new Date(refreshToken + accessTokenExpireTime * 60 * 1000));
        setClaim.accept(builder);
        String accessToken = builder.sign(this.generateAlgorithm(userName));

        if(StringUtils.isNotBlank(tokenMd5)) {
            logger.debug("Cache tokenMd5={}", tokenMd5);
            this.redisTemplate.opsForValue().set(tokenMd5, null, tokenSignCacheTime, TimeUnit.SECONDS);
        }
        this.setNewRefreshToken(refreshTokenKey, refreshToken, accessToken);
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
        if(refreshToken != currentRefreshToken) {
            logger.warn("tokenMd5 = {}", DigestUtils.md5DigestAsHex(token.getBytes()));
            logger.warn("token = {}", token);
            Boolean flag = this.redisTemplate.hasKey(DigestUtils.md5DigestAsHex(token.getBytes()));
            if(flag != null && flag) {
                logger.warn("pings-true");
                return true;
            }

            throw new RefreshTokenExpiredException("The refresh token has expired.");
        }

        //**访问令牌校验
        try {
            return super.verify(token);
        } catch (TokenExpiredException e){
            throw new AccessTokenExpiredException("The access token has expired.");
        }
    }

    //**设置最新的刷新令牌
    private void setNewRefreshToken(String refreshTokenKey, long refreshToken, String accessToken){
        String script = "local value = redis.call('get', KEYS[1]) " +
                        "if not value or value < ARGV[1] then " +
                        "    redis.call('set', KEYS[1], ARGV[1]) " +
                        "    redis.call('expire', KEYS[1], ARGV[2]) " +
                        "end " +
                        "redis.call('set', KEYS[2], ARGV[3]) " +
                        "redis.call('expire', KEYS[2], ARGV[4])";
        String tokenMd5 = DigestUtils.md5DigestAsHex(accessToken.getBytes());
        redisTemplate.<Boolean>execute(new DefaultRedisScript<>(script), Arrays.asList(refreshTokenKey, tokenMd5), refreshToken, refreshTokenExpireTime * 60, null, tokenSignCacheTime);
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
