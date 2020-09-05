# pingsCommons
## 简介
### pingsCommons是一个java工具包
## 项目说明
### pings-java-commons
- java常用工具类
### pings-shiro-jwt
- 基于jwt和shiro的无状态权限认证工具
## 更新记录
- 2019-05-20 搭建pings-shiro-jwt项目
- 2019-11-22 修复在accessToken过期时同一个用户的并发请求，同时请求签名，只有最后一个签名生效，其它的签名会失效的问题
- 2019-12-31 增加在签名中增加其它参数的方法sign(String userName, Map<String, String> params)和sign(String userName, Consumer<JWTCreator.Builder> setClaim);
- 2020-03-21 修复生成多个sheet表格报错的问题
- 2020-09-05 修复pings-shiro-jwt同一个用户并发请求的问题