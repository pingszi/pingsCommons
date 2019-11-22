package cn.pings.jwt.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 *********************************************************
 ** @desc  ： access token过期，refresh token未过期
 ** @author  Pings
 ** @date    2019/5/21
 ** @version v1.0
 * *******************************************************
 */
public class AccessTokenExpiredException extends AuthenticationException {

    public AccessTokenExpiredException(String message) {
        super(message);
    }
}
