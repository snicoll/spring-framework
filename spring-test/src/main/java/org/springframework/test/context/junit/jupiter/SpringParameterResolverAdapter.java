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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.springframework.test.context.junit.jupiter.SpringExtension.findProperlyScopedExtensionContext;

/**
 * {@link ParameterResolver} implementation that invokes registered
 * {@link SpringParameterResolver} implementations.
 *
 * @author Stephane Nicoll
 * @since 7.1
 */
public class SpringParameterResolverAdapter implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (getParameterResolver(parameterContext, extensionContext) != null);
	}

	@Override
	public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Executable executable = parameterContext.getDeclaringExecutable();
		Class<?> testClass = extensionContext.getRequiredTestClass();
		if (executable instanceof Constructor<?> constructor) {
			testClass = constructor.getDeclaringClass();
			extensionContext = findProperlyScopedExtensionContext(testClass, extensionContext);
		}
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(extensionContext);
		return Objects.requireNonNull(getParameterResolver(parameterContext, extensionContext))
				.resolveParameter(applicationContext, parameterContext, extensionContext);
	}

	private @Nullable SpringParameterResolver getParameterResolver(ParameterContext parameterContext, ExtensionContext extensionContext) {
		List<SpringParameterResolver> resolvers = SpringFactoriesLoader.forDefaultResourceLocation()
				.load(SpringParameterResolver.class).stream()
				.sorted(AnnotationAwareOrderComparator.INSTANCE).toList();
		for (SpringParameterResolver resolver : resolvers) {
			if (resolver.supportsParameter(parameterContext, extensionContext)) {
				return resolver;
			}
		}
		return null;
	}
}
