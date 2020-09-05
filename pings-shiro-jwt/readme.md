# pings-shiro-jwt
## 简介
### pings-shiro-jwt是基于jwt和shiro的无状态权限认证工具，可实现如下两种认证方式：
- access token无状态权限认证
    - [原理](https://blog.csdn.net/zhouping118/article/details/89355215)
    - 优点
        - 实现简单
        - 不外部依赖运行环境
        - 如果多个系统之间用户信息和配置的secret相同，某个系统签发的token即可访问所有其它的任意系统
    - 问题
        - 如果token过期时间太短，则每次到期后，都需要用户重新登录
        - 如果token过期时间太长，由于token签发后，在有效期内无法注销，存在安全隐患
- 结合refresh token和access token的无状态权限认证
    - [原理](https://blog.csdn.net/zhouping118/article/details/89355282)
    - 优点
        - 安全性好，可以像session一样管理用户
        - 分布式系统鉴权，如果多个系统之间用户信息和配置的secret相同，某个系统签发的token即可访问所有其它的任意系统
    - 问题
        - 依赖redis存储refresh token，实现多个系统之间的refresh token共享
### [示例：dubbo微服务脚手架](https://github.com/pingszi/pingsSys/tree/master/pings-web-admin/)
## 使用
### 1.配置
#### 1).使用access token方式
- application.yml
```
# 系统管理 config
sys:
  jwt:
    secret: ==SFddfenfV2FuZzkyNjQ1NGRTQkFQSUpXVA==
     # 访问令牌过期时长(分钟)，默认配置600分钟
    access-token:
      expire-time: 300
```
- ShiroConfig.java
```
/**
 *********************************************************
 ** @desc  ： Shiro配置
 ** @author  Pings
 ** @date    2019/1/23
 ** @version v1.0
 * *******************************************************
 */
@Configuration
public class ShiroConfig {

    //**访问令牌过期时间(分钟)
    @Value("${sys.jwt.access-token.expire-time}")
    private long accessTokenExpireTime;
    @Value("${sys.jwt.secret}")
    private String secret;

    @Reference(version = "${sys.service.version}")
    private UserService userService;

    @Bean
    public JwtVerifier verifier(RedisTemplate<String, Object> redisTemplate){
        return new AccessTokenJwtVerifier(secret, accessTokenExpireTime);
    }

    @Bean
    public JwtRealm jwtRealm(JwtVerifier verifier){
        return new JwtRealm(this.userService, verifier);
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(JwtRealm jwtRealm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        //**使用自定义JwtRealm
        manager.setRealm(jwtRealm);

        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        manager.setSubjectDAO(subjectDAO);

        return manager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager, JwtVerifier verifier) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();

        //**添加自定义过滤器jwt
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("jwt", new JwtFilter(verifier));
        factoryBean.setFilters(filterMap);

        factoryBean.setSecurityManager(securityManager);

        //**自定义url规则
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        //不拦截请求swagger-ui页面请求
        filterRuleMap.put("/webjars/**", "anon");
        //jwt过滤器拦截请求
        filterRuleMap.put("/**", "jwt");
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);

        return factoryBean;
    }

    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}

```
#### 2).结合refresh token和access token的方式
- application.yml
```
# 系统管理 config
sys:
  jwt:
      secret: ==SFddfenfV2FuZzkyNjQ1NGRTQkFQSUpXVA==
       # 访问令牌过期时长(分钟)，默认配置5分钟
      access-token:
        expire-time: 5
        # 生成签名后缓存时间(单位s，生成签名后在指定时间内不重新生成新的签名，而使用缓存)，默认10秒
        sign-cache-time: 8
      # 刷新令牌过期时长(分钟)，默认配置60分钟
      refresh-token:
        expire-time: 1000
```
- ShiroConfig.java
```
/**
 *********************************************************
 ** @desc  ： Shiro配置
 ** @author  Pings
 ** @date    2019/1/23
 ** @version v1.0
 * *******************************************************
 */
@Configuration
public class ShiroConfig {

    //**访问令牌过期时间(分钟)
    @Value("${sys.jwt.access-token.expire-time}")
    private long accessTokenExpireTime;
    //**生成签名后缓存时间(单位s，生成签名后在指定时间内不重新生成新的签名，而使用缓存)
    @Value("${sys.jwt.access-token.sign-cache-time}")
    private int tokenSignCacheTime;
    //**刷新信息过期时间(分钟)
    @Value("${sys.jwt.refresh-token.expire-time}")
    private long refreshTokenExpireTime;
    //**密钥
    @Value("${sys.jwt.secret}")
    private String secret;

    @Reference(version = "${sys.service.version}")
    private UserService userService;

    @Bean
    public JwtVerifier verifier(RedisTemplate<String, Object> redisTemplate){
        return RefreshTokenJwtVerifier.Builder.newBuilder(redisTemplate)
                .accessTokenExpireTime(accessTokenExpireTime)
                .refreshTokenExpireTime(refreshTokenExpireTime)
                .tokenSignCacheTime(tokenSignCacheTime)
                .secret(secret)
                .build();
    }

    @Bean
    public JwtRealm jwtRealm(JwtVerifier verifier){
        return new JwtRealm(this.userService, verifier);
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(JwtRealm jwtRealm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        //**使用自定义JwtRealm
        manager.setRealm(jwtRealm);

        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        manager.setSubjectDAO(subjectDAO);

        return manager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager, JwtVerifier verifier) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();

        //**添加自定义过滤器jwt
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("jwt", new JwtFilter(verifier));
        factoryBean.setFilters(filterMap);

        factoryBean.setSecurityManager(securityManager);

        //**自定义url规则
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        //不拦截请求swagger-ui页面请求
        filterRuleMap.put("/webjars/**", "anon");
        //jwt过滤器拦截请求
        filterRuleMap.put("/**", "jwt");
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);

        return factoryBean;
    }

    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
```
### 2.自定义shiro realm
```
/**
 *********************************************************
 ** @desc  ： jwt realm
 ** @author  Pings
 ** @date    2019/5/10
 ** @version v1.0
 * *******************************************************
 */
public class JwtRealm extends AbstractJwtRealm {

    protected UserService userService;

    public JwtRealm(UserService userService, JwtVerifier verifier){
        super(verifier);
        this.userService = userService;
    }

    /**权限验证*/
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        String userName = verifier.getUserName(principals.toString());

        //**获取用户
        User user = this.userService.getByUserName(userName);

        //**用户角色
        Set<String> roles = user.getRoles().stream().map(Role::getCode).collect(toSet());
        authorizationInfo.addRoles(roles);

        //**用户权限
        Set<String> rights = user.getRoles().stream().map(Role::getRights).flatMap(List::stream).map(Right::getCode).collect(toSet());
        authorizationInfo.addStringPermissions(rights);

        return authorizationInfo;
    }

    /**登录验证*/
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        //**获取用户名称
        String userName = verifier.getUserName(token);
        //**用户名称为空
        if (StringUtils.isBlank(userName)) {
            throw new UnknownAccountException("The account in Token is empty.");
        }

        //**获取用户
        User user = this.userService.getByUserName(userName);
        if (user == null) {
            throw new UnknownAccountException("The account does not exist.");
        }

        //**登录认证
        if (verifier.verify(token)) {
            return new SimpleAuthenticationInfo(token, token, "jwtRealm");
        }

        throw new AuthenticationException("Username or password error.");
    }
}
```
### 3.login and logout
```
    /**
     *********************************************************
     ** @desc ： 登录
     ** @author Pings
     ** @date   2019/1/22
     ** @param  userName  用户名称
     ** @param  password  用户密码
     ** @return ApiResponse
     * *******************************************************
     */
    @ApiOperation(value="登录", notes="验证用户名和密码")
    @PostMapping(value = "/account")
    public ApiResponse account(String userName, String password, HttpServletResponse response){
        if(StringUtils.isBlank(userName) || StringUtils.isBlank(password))
            throw new UnknownAccountException("用户名/密码不能为空");

        //**md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        User user = this.userService.getByUserName(userName);
        if(user != null && user.getPassword().equals(password)) {
            JwtUtil.setHttpServletResponse(response, verifier.sign(userName));

            //**用户权限
            Set<String> rights = user.getRoles().stream().map(Role::getRights).flatMap(List::stream).map(Right::getCode).collect(toSet());
            return new ApiResponse(200, "登录成功", rights);
        } else
            return new ApiResponse(400, "用户名/密码错误");
    }

    /**
     *********************************************************
     ** @desc ： 退出登录
     ** @author Pings
     ** @date   2019/3/26
     ** @return ApiResponse
     * *******************************************************
     */
    @ApiOperation(value="退出登录", notes="退出登录")
    @GetMapping(value = "/logout")
    public ApiResponse logout(){
        this.verifier.invalidateSign(this.getCurrentUserName());

        //**退出登录
        SecurityUtils.getSubject().logout();

        return new ApiResponse(200, "退出登录成功");
    }
```
## 更新记录
- 2019-05-20 搭建
- 2019-11-22 修复在accessToken过期时同一个用户的并发请求，同时请求签名，只有最后一个签名生效，其它的签名会失效的问题
- 2020-09-05 修复同一个用户并发请求的问题