/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.aot;

import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Nullable;

/**
 * Thrown when AOT fails to process a bean.
 *
 * @author Stephane Nicoll
 * @since 6.2
 */
@SuppressWarnings("serial")
public class AotBeanProcessingFailedException extends AotProcessingFailedException {

	private final RootBeanDefinition beanDefinition;

	public AotBeanProcessingFailedException(RegisteredBean registeredBean, String msg, @Nullable Throwable cause) {
		super(createErrorMessage(registeredBean, msg), cause);
		this.beanDefinition = registeredBean.getMergedBeanDefinition();
	}

	public AotBeanProcessingFailedException(RegisteredBean registeredBean, String msg) {
		this(registeredBean, msg, null);
	}

	private static String createErrorMessage(RegisteredBean registeredBean, String msg) {
		StringBuilder sb = new StringBuilder("Error processing bean with name '");
		sb.append(registeredBean.getBeanName()).append("'");
		String resourceDescription = registeredBean.getMergedBeanDefinition().getResourceDescription();
		if (resourceDescription != null) {
			sb.append(" defined in ").append(resourceDescription);
		}
		sb.append(": ").append(msg);
		return sb.toString();
	}

	/**
	 * Return the bean definition of the bean that failed to be processed.
	 */
	public RootBeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

}
