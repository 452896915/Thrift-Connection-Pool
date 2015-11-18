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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TServiceClient;

import com.wmz7year.thrift.pool.connection.ThriftConnection;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * 默认连接池获取策略实现类
 * 
 * @Title: DefaultThriftConnectionStrategy.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 下午3:50:58
 * @version V1.0
 */
public class DefaultThriftConnectionStrategy<T extends TServiceClient> extends AbstractThriftConnectionStrategy<T> {
	private static final long serialVersionUID = 142121086900189271L;

	public DefaultThriftConnectionStrategy(ThriftConnectionPool<T> pool) {
		this.pool = pool;
	}

	/*
	 * @see com.wmz7year.thrift.pool.AbstractThriftConnectionStrategy#
	 * getConnectionInternal()
	 */
	@Override
	protected ThriftConnection<T> getConnectionInternal() throws ThriftConnectionPoolException {
		ThriftConnection<T> result = pollConnection();
		// 如果立即获取连接失败 则换一个分区继续获取
		// TODO 设置当连接获取超时返回null？
		if (result == null) {
			int partition = (int) (Thread.currentThread().getId() % this.pool.thriftServerCount);

			ThriftConnectionPartition<T> thriftConnectionPartition = this.pool.partitions.get(partition);

			try {
				result = thriftConnectionPartition.poolFreeConnection(this.pool.connectionTimeoutInMs,
						TimeUnit.MILLISECONDS);
				if (result == null) {

					throw new ThriftConnectionPoolException("Timed out waiting for a free available connection.");
				}
			} catch (InterruptedException e) {
				throw new ThriftConnectionPoolException(e);
			}
		}
		return result;
	}

	/*
	 * @see
	 * com.wmz7year.thrift.pool.ThriftConnectionStrategy#terminateAllConnections
	 * ()
	 */
	@Override
	public void terminateAllConnections() {
		this.terminationLock.lock();
		try {
			for (int i = 0; i < this.pool.thriftServerCount; i++) {
				this.pool.partitions.get(i).setUnableToCreateMoreTransactions(false);
				List<ThriftConnectionHandle<T>> clist = new LinkedList<ThriftConnectionHandle<T>>();
				this.pool.partitions.get(i).getFreeConnections().drainTo(clist);
				for (ThriftConnectionHandle<T> c : clist) {
					this.pool.destroyConnection(c);
				}

			}
		} finally {
			this.terminationLock.unlock();
		}
	}

	/*
	 * @see com.wmz7year.thrift.pool.ThriftConnectionStrategy#pollConnection()
	 */
	@Override
	public ThriftConnection<T> pollConnection() {
		ThriftConnection<T> result = null;
		int partition = (int) (Thread.currentThread().getId() % this.pool.thriftServerCount);

		ThriftConnectionPartition<T> thriftConnectionPartition = this.pool.partitions.get(partition);

		result = thriftConnectionPartition.poolFreeConnection();
		if (result == null) {
			for (int i = 0; i < this.pool.thriftServerCount; i++) {
				if (i == partition) {
					continue;
				}
				result = this.pool.partitions.get(i).poolFreeConnection();
				if (result != null) {
					thriftConnectionPartition = this.pool.partitions.get(i);
					break;
				}
			}
		}

		if (!thriftConnectionPartition.isUnableToCreateMoreTransactions()) {
			this.pool.maybeSignalForMoreConnections(thriftConnectionPartition);
		}
		return result;
	}

}
