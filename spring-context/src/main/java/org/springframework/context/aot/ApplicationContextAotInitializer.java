/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.context.aot;

import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Initializes an {@link GenericApplicationContext} using AOT optimizations if
 * necessary.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public class ApplicationContextAotInitializer {

	private static final Log logger = LogFactory.getLog(ApplicationContextAotInitializer.class);

	@Nullable
	private final ClassLoader classLoader;

	/**
	 * Create a new instance with the specified {@link ClassLoader}.
	 * @param classLoader the classloader to use.
	 */
	public ApplicationContextAotInitializer(@Nullable ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Initialize a context with
	 * @param context
	 * @param initializerClassNames
	 * @return
	 */
	public void initialize(GenericApplicationContext context, String... initializerClassNames) {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing ApplicationContext with AOT");
		}
		for (String initializerClassName : initializerClassNames) {
			if (logger.isTraceEnabled()) {
				logger.trace("Applying " + initializerClassName);
				loadInitializer(initializerClassName).initialize(context);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ApplicationContextInitializer<GenericApplicationContext> loadInitializer(String className) {
		Object initializer = instantiate(className);
		if (!(initializer instanceof ApplicationContextInitializer)) {
			throw new IllegalArgumentException("Not an ApplicationContextInitializer " + className);
		}
		return (ApplicationContextInitializer<GenericApplicationContext>) initializer;
	}

	private Object instantiate(String className) {
		try {
			Class<?> type = ClassUtils.forName(className, this.classLoader);
			return BeanUtils.instantiateClass(type);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Failed to instantiate ApplicationContextInitializer " + className, ex);
		}
	}

}
