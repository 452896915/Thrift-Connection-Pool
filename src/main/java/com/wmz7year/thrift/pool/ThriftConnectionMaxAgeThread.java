/**
 *  				Copyright 2015 Jiang Wei
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.wmz7year.thrift.pool;

import org.apache.thrift.TServiceClient;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.ServiceOrder;

/**
 * thrift连接最大时间检测线程
 * 
 * @Title: ThriftConnectionMaxAgeThread.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 下午1:24:42
 * @version V1.0
 */
public class ThriftConnectionMaxAgeThread<T extends TServiceClient> implements Runnable {

	public ThriftConnectionMaxAgeThread(ThriftConnectionPartition<T> thriftConnectionPartition,
			ThriftConnectionPool<T> thriftConnectionPool, long maxConnectionAge, ServiceOrder serviceOrder) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
