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

package org.springframework.cache.jcache.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheInvocationParameter;

import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.jcache.model.JCacheOperation;

/**
 * The default {@link CacheOperationInvocationContext} implementation used
 * by all interceptors. Also implements {@link CacheInvocationContext} to
 * act as a proper bridge when calling JSR-107 {@link javax.cache.annotation.CacheResolver}
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
public class DefaultCacheInvocationContext<A extends Annotation>
		implements CacheInvocationContext<A>, CacheOperationInvocationContext<JCacheOperation<A>> {

	private final JCacheOperation<A> operation;

	private final Object target;

	private final Object[] args;

	private final CacheInvocationParameter[] allParameters;

	public DefaultCacheInvocationContext(JCacheOperation<A> operation,
			Object target, Object[] args) {
		this.operation = operation;
		this.target = target;
		this.args = args;
		this.allParameters = operation.getAllParameters(args);
	}

	@Override
	public JCacheOperation<A> getOperation() {
		return operation;
	}

	@Override
	public Method getMethod() {
		return operation.getMethod();
	}

	@Override
	public Object[] getArgs() {
		return args.clone();
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return operation.getAnnotations();
	}

	@Override
	public A getCacheAnnotation() {
		return operation.getCacheAnnotation();
	}

	@Override
	public String getCacheName() {
		return operation.getCacheName();
	}

	@Override
	public Object getTarget() {
		return target;
	}

	@Override
	public CacheInvocationParameter[] getAllParameters() {
		return allParameters.clone();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		throw new IllegalArgumentException("Could not unwrap to '" + cls.getName() + "'");
	}

}
