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

import java.lang.reflect.Parameter;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import org.springframework.context.ApplicationContext;

/**
 * Extension of {@link ParameterResolver} for parameters that may require the
 * {@link ApplicationContext} to be resolved.
 *
 * <p>Implementations should be registered in {@code META-INF/spring.factories} and
 * have their {@link #supportsParameter(ParameterContext, ExtensionContext)}
 * implementation to account for the fact that a given type may not be on the
 * classpath.
 *
 * <p>Adding {@link org.springframework.core.Ordered} to your resolver implementation
 * allows you to prioritize that listener amongst other resolvers. The first resolver
 * that claims to support a given parameter will be used.
 *
 * @author Stephane Nicoll
 * @since 7.1
 * @see SpringExtension
 */
public interface SpringParameterResolver {

	/**
	 * Determine if this resolver supports resolution of an argument for the
	 * {@link Parameter} in the supplied {@link ParameterContext} for the supplied
	 * {@link ExtensionContext}.
	 * <p>The {@link java.lang.reflect.Method} or {@link java.lang.reflect.Constructor}
	 * in which the parameter is declared can be retrieved via
	 * {@link ParameterContext#getDeclaringExecutable()}.
	 * @param parameterContext the context for the parameter for which an argument should
	 * be resolved
	 * @param extensionContext the extension context for the {@code Executable}
	 * about to be invoked
	 * @return {@code true} if this resolver can resolve an argument for the parameter
	 * @see ParameterResolver#resolveParameter(ParameterContext, ExtensionContext)
	 */
	boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException;

	/**
	 * Resolve an argument for the {@link Parameter} in the supplied {@link ParameterContext}
	 * for the supplied {@link ExtensionContext}, using the current {@link ApplicationContext}.
	 * <p>This method is only called by the framework if {@link #supportsParameter}
	 * previously returned {@code true} for the same {@link ParameterContext}
	 * and {@link ExtensionContext}.
	 * <p>The {@link java.lang.reflect.Method} or {@link java.lang.reflect.Constructor}
	 * in which the parameter is declared can be retrieved via
	 * {@link ParameterContext#getDeclaringExecutable()}.
	 * @param applicationContext the current application context
	 * @param parameterContext the context for the parameter for which an argument should
	 * be resolved
	 * @param extensionContext the extension context for the {@code Executable}
	 * about to be invoked
	 * @return the resolved argument for the parameter; may only be {@code null} if the
	 * parameter type is not a primitive
	 * @see ParameterResolver#resolveParameter(ParameterContext, ExtensionContext)
	 */
	@Nullable Object resolveParameter(ApplicationContext applicationContext, ParameterContext parameterContext,
			ExtensionContext extensionContext) throws ParameterResolutionException;

}
