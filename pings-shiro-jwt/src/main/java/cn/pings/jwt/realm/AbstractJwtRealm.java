package cn.pings.jwt.realm;

import cn.pings.jwt.JwtToken;
import cn.pings.jwt.verifier.JwtVerifier;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.AuthorizingRealm;

/**
 *********************************************************
 ** @desc  ： jwt realm
 ** @author  Pings
 ** @date    2019/1/23
 ** @version v1.0
 * *******************************************************
 */
public abstract class AbstractJwtRealm extends AuthorizingRealm {

    protected JwtVerifier verifier;

    public AbstractJwtRealm(JwtVerifier verifier){
        this.verifier = verifier;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }
}
