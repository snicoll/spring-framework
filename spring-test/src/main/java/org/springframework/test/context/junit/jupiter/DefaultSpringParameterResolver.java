/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.test.context.junit.jupiter;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.support.PropertyProvider;
import org.springframework.test.context.support.TestConstructorUtils;
import org.springframework.util.Assert;

/**
 * {@link SpringParameterResolver} implementation that resolves core concepts
 * managed by {@link SpringExtension}. This resolver is ordered with a value
 * of {@value #ORDER}. Consider providing a higher precedence to any
 * implementation that should take precedence over the defaults.
 *
 * @author Stephane Nicoll
 * @since 7.1
 */
class DefaultSpringParameterResolver implements SpringParameterResolver, Ordered {

	private static final int ORDER = 0;

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		Parameter parameter = parameterContext.getParameter();
		Class<?> parameterType = parameter.getType();
		Executable executable = parameter.getDeclaringExecutable();
		PropertyProvider junitPropertyProvider = propertyName ->
				extensionContext.getConfigurationParameter(propertyName).orElse(null);
		return (TestConstructorUtils.isAutowirableConstructor(executable, junitPropertyProvider) ||
				ApplicationContext.class.isAssignableFrom(parameterType) ||
				supportsApplicationEvents(parameterType, executable) ||
				ParameterResolutionDelegate.isAutowirable(parameter, parameterContext.getIndex()));
	}

	@Override
	public @Nullable Object resolveParameter(ApplicationContext applicationContext, ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		Parameter parameter = parameterContext.getParameter();
		int index = parameterContext.getIndex();
		Class<?> testClass = extensionContext.getRequiredTestClass();
		return ParameterResolutionDelegate.resolveDependency(parameter, index, testClass,
				applicationContext.getAutowireCapableBeanFactory());
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	private boolean supportsApplicationEvents(Class<?> parameterType, Executable executable) {
		if (ApplicationEvents.class.isAssignableFrom(parameterType)) {
			Assert.isTrue(executable instanceof Method,
					"ApplicationEvents can only be injected into test and lifecycle methods");
			return true;
		}
		return false;
	}

}
