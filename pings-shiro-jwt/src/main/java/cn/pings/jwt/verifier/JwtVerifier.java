package cn.pings.jwt.verifier;

import cn.pings.commons.util.jwt.JwtUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.Payload;
import org.springframework.util.DigestUtils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Consumer;

import static cn.pings.jwt.verifier.RefreshTokenJwtVerifier.REFRESH_TOKEN_PREFIX;
import static java.util.stream.Collectors.toMap;

/**
 *********************************************************
 ** @desc  ： Jwt校验器
 ** @author  Pings
 ** @date    2019/5/17
 ** @version v1.0
 * *******************************************************
 */
public interface JwtVerifier {

    //**用户名称的key
    String USER_NAME = "userName";

    /**
     *********************************************************
     ** @desc ： 生成令牌
     ** @author Pings
     ** @date   2019/5/17
     ** @param  userName    用户名
     ** @return String
     * *******************************************************
     */
    String sign(String userName);

    /**
     *********************************************************
     ** @desc ： 生成令牌
     ** @author Pings
     ** @date   2019/12/31
     ** @param  userName    用户名
     ** @param  params      其它参数
     ** @return String
     * *******************************************************
     */
    String sign(String userName, Map<String, String> params);

    /**
     *********************************************************
     ** @desc ： 生成令牌
     ** @author Pings
     ** @date   2019/12/31
     ** @param  userName    用户名
     ** @param  setClaim    函数，设置参数
     ** @return String
     * *******************************************************
     */
    String sign(String userName, Consumer<JWTCreator.Builder> setClaim);

    /**
     *********************************************************
     ** @desc ： 生成令牌
     ** @author Pings
     ** @date   2020/9/3
     ** @param  userName    用户名
     ** @param  setClaim    函数，设置参数
     ** @param  tokenMd5   md5加密后的访问令牌
     ** @return String
     * *******************************************************
     */
    default String sign(String userName, Consumer<JWTCreator.Builder> setClaim, String tokenMd5){
        throw new RuntimeException("This method is not supported");
    }

    /**
     *********************************************************
     ** @desc ： 校验token
     ** @author Pings
     ** @date   2019/5/17
     ** @param  token       令牌
     ** @return boolean
     * *******************************************************
     */
    default boolean verify(String token) {
        JWTVerifier verifier = JWT.require(this.generateAlgorithm(this.getUserName(token))).build();
        verifier.verify(token);
        return true;
    }

    /**
     *********************************************************
     ** @desc ： 使签名无效，默认的签名在有效期内无法失效
     ** @author Pings
     ** @date   2019/5/20
     ** @param  userName    用户名
     ** @return String
     * *******************************************************
     */
    default void invalidateSign(String userName){ }

    /**
     *********************************************************
     ** @desc ： 根据secret生成jwt算法
     ** @author Pings
     ** @date   2019/5/17
     ** @param  userName 用户名称
     ** @return Algorithm
     * *******************************************************
     */
    default Algorithm generateAlgorithm(String userName){
        return Algorithm.HMAC256(this.generateUniqueSecret(userName));
    }

    /**
     *********************************************************
     ** @desc ： 根据基础密钥和用户名称生成唯一的密钥
     ** @author Pings
     ** @date   2019/5/17
     ** @param  userName 用户名称
     ** @return String
     * *******************************************************
     */
    default String generateUniqueSecret(String userName){
        return userName + "pingssys";
    }

    /**
     *********************************************************
     ** @desc ：根据token获取用户名称
     ** @author Pings
     ** @date   2019/5/17
     ** @param  token  令牌
     ** @return String
     * *******************************************************
     */
    default String getUserName(String token) {
        return JwtUtil.getValue(token, USER_NAME).asString();
    }

    /**
     *********************************************************
     ** @desc ：根据旧token生成新签名
     ** @author Pings
     ** @date   2020/01/02
     ** @param  token  令牌
     ** @return String
     * *******************************************************
     */
    default String signByOldToken(String token){
        Map<String, Claim> params = JwtUtil.decodeToken(token, Payload::getClaims);
        assert params != null;

        Map<String, String> paramMap = params.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(USER_NAME))
            .filter(entry -> !entry.getKey().equals(PublicClaims.EXPIRES_AT))
            .filter(entry -> !entry.getKey().equals(REFRESH_TOKEN_PREFIX))
            .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().asString()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        return this.sign(params.get(USER_NAME).asString(), builder -> paramMap.forEach(builder::withClaim), DigestUtils.md5DigestAsHex(token.getBytes()));
    }
}
