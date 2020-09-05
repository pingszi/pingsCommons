package cn.pings.jwt.verifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *********************************************************
 ** @desc  ： Jwt校验器
 ** @author  Pings
 ** @date    2019/5/17
 ** @version v1.0
 * *******************************************************
 */
public abstract class AbstractJwtVerifier implements JwtVerifier {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    //**访问令牌过期时间(分钟)，默认5分钟
    protected long accessTokenExpireTime = 5;

    //**基础密钥，默认"pingssys"
    protected String secret = "pingssys";

    @Override
    public String generateUniqueSecret(String userName){
        return userName + secret;
    }
}
