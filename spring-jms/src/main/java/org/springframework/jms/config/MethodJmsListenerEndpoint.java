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

import java.lang.reflect.Method;

/**
 * A {@link JmsListenerEndpoint} providing the method to invoke to process
 * an incoming message for this endpoint.
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
public class MethodJmsListenerEndpoint extends JmsListenerEndpoint {

	private Object bean;

	private Method method;

	private String responseDestination;

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	/**
	 * Return the name of the default response destination to send response messages to.
	 */
	public String getResponseDestination() {
		return responseDestination;
	}

	public void setResponseDestination(String responseDestination) {
		this.responseDestination = responseDestination;
	}

	@Override
	protected StringBuilder getEndpointDescription() {
		return super.getEndpointDescription()
				.append(" | bean='")
				.append(this.bean)
				.append("'")
				.append(" | method='")
				.append(this.method)
				.append("'");
	}
}
