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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 连接池内部连接数量监控线程
 * 
 * @Title: PoolWatchThread.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 下午1:30:01
 * @version V1.0
 */
public class PoolWatchThread<T extends TServiceClient> implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(PoolWatchThread.class);

	/**
	 * 连接池对象
	 */
	private ThriftConnectionPool<T> thriftConnectionPool;

	/**
	 * 连接分区对象
	 */
	private ThriftConnectionPartition<T> thriftConnectionPartition;

	/**
	 * 是否为懒加载
	 */
	protected boolean lazyInit;

	/**
	 * 连接池连接数比例阈值
	 */
	private int poolAvailabilityThreshold;

	/**
	 * 获取连接失败时的重试间隔事件
	 */
	private long acquireRetryDelayInMs = 1000L;

	public PoolWatchThread(ThriftConnectionPartition<T> thriftConnectionPartition,
			ThriftConnectionPool<T> thriftConnectionPool) {
		this.thriftConnectionPool = thriftConnectionPool;
		this.thriftConnectionPartition = thriftConnectionPartition;
		this.lazyInit = this.thriftConnectionPool.getConfig().isLazyInit();
		this.acquireRetryDelayInMs = this.thriftConnectionPool.getConfig().getAcquireRetryDelayInMs();
		this.poolAvailabilityThreshold = this.thriftConnectionPool.getConfig().getPoolAvailabilityThreshold();
	}

	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int maxNewConnections = 0;

		while (true) {
			maxNewConnections = 0;
			try {
				if (this.lazyInit) { // 无视第一次的信号量
					this.thriftConnectionPartition.getPoolWatchThreadSignalQueue().take();
				}

				maxNewConnections = this.thriftConnectionPartition.getMaxConnections()
						- this.thriftConnectionPartition.getCreatedConnections();

				while (maxNewConnections == 0 || (this.thriftConnectionPartition.getAvailableConnections() * 100
						/ this.thriftConnectionPartition.getMaxConnections() > this.poolAvailabilityThreshold)) {
					if (maxNewConnections == 0) {
						this.thriftConnectionPartition.setUnableToCreateMoreTransactions(true);
					}

					this.thriftConnectionPartition.getPoolWatchThreadSignalQueue().take();
					maxNewConnections = this.thriftConnectionPartition.getMaxConnections()
							- this.thriftConnectionPartition.getCreatedConnections();

				}

				if (maxNewConnections > 0 && !this.thriftConnectionPool.poolShuttingDown) {
					fillConnections(Math.min(maxNewConnections, this.thriftConnectionPartition.getAcquireIncrement()));

					if (this.thriftConnectionPartition.getCreatedConnections() < this.thriftConnectionPartition
							.getMinConnections()) {
						fillConnections(this.thriftConnectionPartition.getMinConnections()
								- this.thriftConnectionPartition.getCreatedConnections());

					}
				}

				if (this.thriftConnectionPool.poolShuttingDown) {
					return;
				}

			} catch (InterruptedException e) {
				// 发生中断事件 停止运行监听器
				if (logger.isDebugEnabled()) {
					logger.debug("Terminating pool watch thread");
				}
				return;
			}
		}
	}

	/**
	 * 触发创建连接的方法<br>
	 * 新的连接会添加到分区中
	 * 
	 * @param connectionsToCreate
	 *            需要创建的连接数
	 * @throws InterruptedException
	 *             当添加操作被中断的时候抛出该异常
	 */
	private void fillConnections(int connectionsToCreate) throws InterruptedException {
		try {
			for (int i = 0; i < connectionsToCreate; i++) {
				if (this.thriftConnectionPool.poolShuttingDown) {
					break;
				}
				this.thriftConnectionPartition.addFreeConnection(new ThriftConnectionHandle<T>(null,
						this.thriftConnectionPartition, this.thriftConnectionPool, false));
			}
		} catch (Exception e) {
			logger.error("Error in trying to obtain a connection. Retrying in " + this.acquireRetryDelayInMs + "ms", e);
			Thread.sleep(this.acquireRetryDelayInMs);
		}
	}

}
