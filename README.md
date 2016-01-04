#Thrift连接池实现

[![Join the chat at https://gitter.im/wmz7year/Thrift-Connection-Pool](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/wmz7year/Thrift-Connection-Pool?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/wmz7year/Thrift-Connection-Pool.svg)](https://travis-ci.org/wmz7year/Thrift-Connection-Pool) 

##特性	
* 1、支持服务器之间的负载均衡<br/>
* 2、每个服务器拥有一个独立的连接分区 所有的连接分区合并一起为整个连接池<br/>
* 3、连接池支持自动创建连接、管理超时连接、管理失效连接<br/>
* 4、支持服务器列表动态增加或者移除<br/>
* 5、支持自动调取ping方法(在thrift描述文件添加方法void ping(),)检测连接可用性<br/>
* 6、支持当服务不可用时自动将对应的服务器剔除连接池的功能<br/>
* 7、添加多服务接口支持<br/>

###下载
>&lt;dependency&gt;     
>&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&lt;groupId&gt;com.github.wmz7year&lt;/groupId&gt;    
>&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&lt;artifactId&gt;ThriftConnectionPool&lt;/artifactId&gt;    
>&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&lt;version&gt;1.0.5-RELEASE&lt;/version&gt;    
>&lt;/dependency&gt;
	
	
###示例
####单服务示例
	ThriftConnectionPoolConfig config = new ThriftConnectionPoolConfig();
	config.setConnectTimeout(3000);
	config.setThriftProtocol(TProtocolType.BINARY);
	config.setClientClass(Example.Client.class);
	config.addThriftServer("127.0.0.1", 9119);
	config.setMaxConnectionPerServer(2);
	config.setMinConnectionPerServer(1);
	config.setIdleMaxAge(2, TimeUnit.SECONDS);
	config.setMaxConnectionAge(2);
	config.setLazyInit(false);
	try {
		ThriftConnectionPool<Example.Client> pool = new ThriftConnectionPool<Example.Client>(config);
		Example.Client client = pool.getConnection().getClient();
		client.ping();
		pool.close();
	} catch (ThriftConnectionPoolException e) {
		e.printStackTrace();
	} catch (TException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}

####多接口服务示例
	ThriftConnectionPoolConfig config = new ThriftConnectionPoolConfig(ThriftServiceType.MULTIPLEXED_INTERFACE);
	config.setConnectTimeout(3000);
	config.setThriftProtocol(TProtocolType.BINARY);
	config.addThriftServer("127.0.0.1", 9119);
	config.addThriftClientClass("other", Other.Client.class);
	config.addThriftClientClass("example", Example.Client.class);

	config.setMaxConnectionPerServer(2);
	config.setMinConnectionPerServer(1);
	config.setIdleMaxAge(2, TimeUnit.SECONDS);
	config.setMaxConnectionAge(2);
	config.setLazyInit(false);
	config.setAcquireIncrement(2);
	config.setAcquireRetryDelay(2000);

	config.setAcquireRetryAttempts(1);
	config.setMaxConnectionCreateFailedCount(1);
	config.setConnectionTimeoutInMs(5000);

	config.check();

	ThriftConnectionPool<TServiceClient> pool = new ThriftConnectionPool<TServiceClient>(config);
	ThriftConnection<TServiceClient> connection = pool.getConnection();
	// example service
	com.wmz7year.thrift.pool.example.Example.Client exampleServiceClient = connection.getClient("example",
			Example.Client.class);
	exampleServiceClient.ping();

	// other service
	com.wmz7year.thrift.pool.example.Other.Client otherServiceClient = connection.getClient("other",
			Other.Client.class);
	otherServiceClient.ping();
	pool.close();



####接下来需要完善内容：
 1、补充文档<br/>
 2、补充性能测试<br/>
 3、完善使用例子<br/>
 4、操作重试机制?<br/>


