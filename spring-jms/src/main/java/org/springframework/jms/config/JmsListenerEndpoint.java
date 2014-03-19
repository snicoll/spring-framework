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

/**
 * Base model for a JMS listener endpoint. Can be used against a
 * {@link org.springframework.jms.annotation.JmsListenerConfigurer JmsListenerConfigurer} to
 * register endpoints programmatically.
 *
 * @author Stephane Nicoll
 * @see MethodJmsListenerEndpoint
 * @see SimpleJmsListenerEndpoint
 */
public abstract class JmsListenerEndpoint {

	private String id;

	private String destination;

	private boolean queue = true;

	private String subscription;

	private String selector;


	/**
	 * Return the id of the listener container.
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Return the name of the destination for this endpoint.
	 */
	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * Return {@code true} if this endpoint is managed by a queue.
	 */
	public boolean isQueue() {
		return queue;
	}

	/**
	 * Specify if the destination is a queue.
	 */
	public void setQueue(boolean queue) {
		this.queue = queue;
	}

	/**
	 * Return the name for the durable subscription, if any.
	 */
	public String getSubscription() {
		return subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	/**
	 * Return the JMS message selector expression, if any.
	 * <p>See the JMS specification for a detailed definition of selector expressions.
	 */
	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	@Override
	public String toString() {
		return getEndpointDescription().toString();
	}

	/**
	 * Return a description for this endpoint.
	 * <p>Available to subclasses, for inclusion in their {@code toString()} result.
	 */
	protected StringBuilder getEndpointDescription() {
		StringBuilder result = new StringBuilder();
		return result.append(getClass().getSimpleName())
				.append("[")
				.append(this.id)
				.append("] destination=")
				.append(this.destination)
				.append(" | queue='")
				.append(this.queue)
				.append("' | subscription='")
				.append(this.subscription)
				.append(" | selector='")
				.append(this.selector)
				.append("'");
	}

}
