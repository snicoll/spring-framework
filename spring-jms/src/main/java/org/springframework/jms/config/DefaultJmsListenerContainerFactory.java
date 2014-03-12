/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jms.config;

import java.util.concurrent.Executor;

import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A {@link JmsListenerContainerFactory} implementation to build regular
 * {@link DefaultMessageListenerContainer}.
 * <p>This should be the default for most users and a good transition
 * paths for those that are used to build such container definition
 * manually.
 *
 * @author Stephane Nicoll
 */
public class DefaultJmsListenerContainerFactory
		extends AbstractJmsListenerContainerFactory<DefaultMessageListenerContainer> {

	private Executor taskExecutor;

	private PlatformTransactionManager transactionManager;

	private Integer cacheLevel;

	private String concurrency;

	private Integer maxMessagesPerTask;

	private Long receiveTimeout;

	private Long recoveryInterval;

	/**
	 * @see DefaultMessageListenerContainer#setTaskExecutor(java.util.concurrent.Executor)
	 */
	public void setTaskExecutor(Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * @see DefaultMessageListenerContainer#setTransactionManager(PlatformTransactionManager)
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * @see DefaultMessageListenerContainer#setCacheLevel(int)
	 */
	public void setCacheLevel(Integer cacheLevel) {
		this.cacheLevel = cacheLevel;
	}

	/**
	 * @see DefaultMessageListenerContainer#setConcurrency(String)
	 */
	public void setConcurrency(String concurrency) {
		this.concurrency = concurrency;
	}

	/**
	 * @see DefaultMessageListenerContainer#setMaxMessagesPerTask(int)
	 */
	public void setMaxMessagesPerTask(Integer maxMessagesPerTask) {
		this.maxMessagesPerTask = maxMessagesPerTask;
	}

	/**
	 * @see DefaultMessageListenerContainer#setReceiveTimeout(long)
	 */
	public void setReceiveTimeout(Long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	/**
	 * @see DefaultMessageListenerContainer#setRecoveryInterval(long)
	 */
	public void setRecoveryInterval(Long recoveryInterval) {
		this.recoveryInterval = recoveryInterval;
	}

	@Override
	protected DefaultMessageListenerContainer createContainerInstance() {
		return new DefaultMessageListenerContainer();
	}

	@Override
	protected void initializeContainer(DefaultMessageListenerContainer container) {
		if (taskExecutor != null) {
			container.setTaskExecutor(taskExecutor);
		}
		if (transactionManager != null) {
			container.setTransactionManager(transactionManager);
		}
		if (cacheLevel != null) {
			container.setCacheLevel(cacheLevel);
		}
		if (concurrency != null) {
			container.setConcurrency(concurrency);
		}
		if (maxMessagesPerTask != null) {
			container.setMaxMessagesPerTask(maxMessagesPerTask);
		}
		if (receiveTimeout != null) {
			container.setReceiveTimeout(receiveTimeout);
		}
		if (recoveryInterval != null) {
			container.setRecoveryInterval(recoveryInterval);
		}
	}

}
