<h1>Thrift连接池实现</h1>

目标：<br/>
  1、支持池化管理thrift客户端连接<br/>
  2、支持thrift服务器的负载均衡<br/>
  3、支持thrift服务器列表的动态管理<br/>

<h1>示例</h1>
<strong>https://github.com/wmz7year/Thrift-Connection-Pool/blob/master/src/test/java/com/wmz7year/thrift/pool/config/TestPool.java</strong>

<h1>使用</h1>
	maven中央仓库发布审核中。。。

<h1>特性</h1>	
  1、支持服务器之间的负载均衡<br/>
  2、每个服务器拥有一个独立的连接分区 所有的连接分区合并一起为整个连接池<br/>
  3、连接池支持自动创建连接、管理超时连接、管理失效连接<br/>

<h1>接下来需要完善内容：</h1>
 1、服务器列表变化时连接分区相应的也变化<br/>
 2、建议使用者添加ping方法检测连接
 3、代码格式整理<br/>
 4、补充单元测试<br/>
 5、补充文档<br/>
 6、补充性能测试<br/>


