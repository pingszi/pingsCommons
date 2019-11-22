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