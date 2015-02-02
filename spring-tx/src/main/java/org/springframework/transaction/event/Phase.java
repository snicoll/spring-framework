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

import org.springframework.transaction.support.TransactionSynchronization;

/**
 * The phase at which a transactional event listener applies.
 *
 * @author Stephane Nicoll
 * @since 4.2.0
 * @see TransactionalEventListener
 */
public enum Phase {

	/**
	 * Fire the event before transaction commit. If no transaction is running,
	 * the event is fired immediately.
	 * @see TransactionSynchronization#beforeCommit(boolean)
	 */
	BEFORE_COMMIT,

	/**
	 * Fire the event after the commit has completed successfully. If no
	 * transaction is running, the event is fired immediately.
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_COMMITTED
	 */
	AFTER_COMMIT,

	/**
	 * Fire the event if the transaction has rolled back. As a result,
	 * such event won't be fired at all if there is no transaction.
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_ROLLED_BACK
	 */
	AFTER_ROLLBACK

}
