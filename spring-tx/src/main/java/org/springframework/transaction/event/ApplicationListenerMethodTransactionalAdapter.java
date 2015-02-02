/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.transaction.event;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * {@link SmartApplicationListener} adapter that delegates the processing of
 * an event to a {@link TransactionalEventListener} annotated method. Supports
 * the exact same features as any regular {@link EventListener} annotated method
 * but is aware of the transactional context of the event publisher.
 * <p>
 * Processing of {@link TransactionalEventListener} is enabled automatically when
 * Spring's transaction management is enabled. For other cases, registering a
 * bean of type {@link TransactionalEventListenerFactory} is required.
 *
 * @author Stephane Nicoll
 * @since 4.2.0
 * @see ApplicationListenerMethodAdapter
 * @see TransactionalEventListener
 */
class ApplicationListenerMethodTransactionalAdapter extends ApplicationListenerMethodAdapter {

	private final Phase phase;

	public ApplicationListenerMethodTransactionalAdapter(ApplicationContext applicationContext, String beanName,
			Class<?> targetClass, Method method) {
		super(applicationContext, beanName, targetClass, method);
		this.phase = determinePhase(method);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronization transactionSynchronization = createTransactionSynchronization(event);
			TransactionSynchronizationManager.registerSynchronization(transactionSynchronization);
		}
		else if (phase != Phase.AFTER_ROLLBACK) { // AFTER_ROLLBACK can't be processed outside a transaction
			processEvent(event);
		}
	}

	private TransactionSynchronization createTransactionSynchronization(ApplicationEvent event) {
		return new TransactionSynchronizationEventAdapter(this, event, this.phase);
	}

	static Phase determinePhase(Method method) {
		TransactionalEventListener annotation = AnnotationUtils
				.findAnnotation(method, TransactionalEventListener.class);
		if (annotation == null) {
			throw new IllegalStateException("No TransactionalEventListener annotation found ou '" + method + "'");
		}
		return annotation.phase();
	}


	private static class TransactionSynchronizationEventAdapter extends TransactionSynchronizationAdapter {

		private final ApplicationListenerMethodAdapter listener;

		private final ApplicationEvent event;

		private final Phase phase;

		protected TransactionSynchronizationEventAdapter(ApplicationListenerMethodAdapter listener,
				ApplicationEvent event, Phase phase) {

			this.listener = listener;
			this.event = event;
			this.phase = phase;
		}

		@Override
		public void beforeCommit(boolean readOnly) {
			if (phase == Phase.BEFORE_COMMIT) {
				processEvent();
			}
		}

		@Override
		public void afterCompletion(int status) {
			if (phase == Phase.AFTER_COMMIT && status == STATUS_COMMITTED) {
				processEvent();
			}
			else if (phase == Phase.AFTER_ROLLBACK && status == STATUS_ROLLED_BACK) {
				processEvent();
			}
		}

		@Override
		public int getOrder() {
			return this.listener.getOrder();
		}

		protected void processEvent() {
			this.listener.processEvent(this.event);
		}
	}

}
