# LarkCloud
spring cloud基础工程搭建
Lark-EasyJoinIntf/lark/LarkClond下为研究例子：
1.eurekaserver为注册中心
2.eurekaclient2为具体的服务提供例子
3.feign为消费工程
4.entites为代码规范公用类



https://github.com/117561271下的LarkCloud工程为可以在开发中实际使用的工程：
1.ApiGateway为zuul网关工程，网关路径配置/api-xxx/**
2.comm-utils为规范化的通用类及工具
3.ConfigCenter为配置中心
4.RegistCenter为注册中心
5.ServiceProvider为服务提供者集合：包括两个配置中心spring-cloud-bus的两个获取例子

服务启动及测试：
1.启动Lark-EasyJoinIntf/lark/LarkClond下的eurekaserver、eurekaclient2、feign工程
2.启动ApiGateway工程
3.测试网关路由、权限token、标准入参及标准返回
    TOKEN通过jwt生成，需要提供用户登录接口生成返回该token，对于用户的帐号字段默认为sessionid，
    可以在application.properties里通过以下属性配置相关信息
        ##JWT Token密码，正式服务请修改该密码
        lark.jwt.secret=123456
        ##登录信息失效时间为30分钟
        lark.user.expiration.time=1800000
        ##登录用户信息需要包含的帐号属性
		lark.user.attr.keyname=sessionid
	用户登录获取TOKEN：http://localhost:8080/api-admin/vip/login
        参数：{"account":"180****06**", "name":"lark","password":"1128"}
        返回：{"status": 0,
               "message": "请求成功",
               "data": "token value" }
	测试时可以通过运行comm-utils包下JWTTokenUtil的main方法生成

	备注：放行请求以实体作为入参，区别于带token的请求；
	    以/vip/*开头的为不做过滤的地址，用于登录及注册等未登录场景下使用，请不要在其他需要权限控制的请求地址中使用


	GET请求：http://localhost:8080/api-admin/hi?name=lark
	返回{"message":"[token]参数缺失或为空","status":-1001} //需要token参数

	一般的POST请求：http://localhost:8080/api-admin/sethi
	参数{"token":"", "param":{"name":"lark","sex":"M"}}}    后端对应接收对象BaseParamEntity

	分页查询：http://localhost:8080/api-admin/getUserForPage
	POST方式，JSON格式，入参格式：{"token":"", "param":{"pageSize":20, "currPage":1}}  后端对应接收对象BaseParamEntity
	

	
4.测试配置中心
4.1安装rabbitmq
4.2启动https://github.com/117561271/LarkCloud下的RegistCenter为注册中心、ConfigCenter为配置中心、ServiceProvider的Client工程
4.3浏览器请求http://localhost:9001/hi 和 http://localhost:9002/hi 获取到版本号
4.4去git修改配置文件的版本号，然后请求http://localhost:9001/actuator/bus-refresh，再请求http://localhost:9001/hi 和 http://localhost:9002/hi 看版本号是否更新了
	
	
开发规范
comm-utils工程
#1.状态值都定义在com.lark.cloud.utils.entity的Status下，或分模块定义
#2.POST方式请求的 @RequestBody 参数统一为com.lark.cloud.utils.entity的BaseParamEntity
#  例：@PostMapping(value = "/sethi")
#        public Result<UserInfo> setHi(@RequestBody BaseParamEntity<UserInfo> user) {
#            return schedualServiceHi.setHiFromClientOne( user.getParam() );
#        }
#  备注：放行请求以实体作为入参，区别于带token的请求，例如登录接口
#3.统一返回com.lark.cloud.utils.entity的Result<?>
#4.分页统一返回Result<Page<?>>
