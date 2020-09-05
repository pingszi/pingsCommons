package cn.pings.jwt.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 *********************************************************
 ** @desc  ： refresh token过期
 ** @author  Pings
 ** @date    2019/5/21
 ** @version v1.0
 * *******************************************************
 */
public class RefreshTokenExpiredException extends AuthenticationException {

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
