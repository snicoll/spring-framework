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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

import javax.cache.annotation.CacheDefaults;
import java.lang.reflect.Method;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.jcache.AbstractJCacheTests;
import org.springframework.cache.jcache.model.BaseKeyCacheOperation;
import org.springframework.cache.jcache.model.CachePutOperation;
import org.springframework.cache.jcache.model.CacheRemoveAllOperation;
import org.springframework.cache.jcache.model.CacheRemoveOperation;
import org.springframework.cache.jcache.model.CacheResultOperation;
import org.springframework.cache.jcache.model.JCacheOperation;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author Stephane Nicoll
 */
public class AnnotationCacheOperationSourceTests extends AbstractJCacheTests {

	private final DefaultJCacheOperationSource source = new DefaultJCacheOperationSource();

	@Before
	public void setUp() {
		source.setApplicationContext(new StaticApplicationContext());
		source.setKeyGenerator(defaultKeyGenerator);
		source.setCacheResolver(defaultCacheResolver);
		source.setExceptionCacheResolver(defaultExceptionCacheResolver);
		source.afterPropertiesSet();
	}

	@Test
	public void cache() {
		CacheResultOperation op = getDefaultCacheOperation(CacheResultOperation.class, String.class);
		assertDefaults(op);
		assertNull("Exception caching not enabled so resolver should not be set", op.getExceptionCacheResolver());
	}

	@Test
	public void cacheWithException() {
		CacheResultOperation op = getDefaultCacheOperation(CacheResultOperation.class, String.class, boolean.class);
		assertDefaults(op);
		assertEquals(defaultExceptionCacheResolver, op.getExceptionCacheResolver());
		assertEquals("exception", op.getExceptionCacheName());
	}

	@Test
	public void put() {
		CachePutOperation op = getDefaultCacheOperation(CachePutOperation.class, String.class, Object.class);
		assertDefaults(op);
	}

	@Test
	public void remove() {
		CacheRemoveOperation op = getDefaultCacheOperation(CacheRemoveOperation.class, String.class);
		assertDefaults(op);
	}

	@Test
	public void removeAll() {
		CacheRemoveAllOperation op = getDefaultCacheOperation(CacheRemoveAllOperation.class);
		assertEquals(defaultCacheResolver, op.getCacheResolver());
	}

	@Test
	public void noAnnotation() {
		assertNull(getCacheOperation(AnnotatedJCacheableService.class, name.getMethodName()));
	}

	@Test
	public void multiAnnotations() {
		thrown.expect(IllegalStateException.class);
		getCacheOperation(AnnotatedJCacheableService.class, name.getMethodName());
	}

	@Test
	public void defaultCacheNameWithCandidate() {
		Method m = ReflectionUtils.findMethod(Object.class, "toString");
		assertEquals("foo", source.determineCacheName(m, null, "foo"));
	}

	@Test
	public void defaultCacheNameWithDefaults() {
		Method m = ReflectionUtils.findMethod(Object.class, "toString");
		CacheDefaults mock = mock(CacheDefaults.class);
		given(mock.cacheName()).willReturn("");
		assertEquals("java.lang.Object.toString()", source.determineCacheName(m, mock, ""));
	}

	@Test
	public void defaultCacheNameNoDefaults() {
		Method m = ReflectionUtils.findMethod(Object.class, "toString");
		assertEquals("java.lang.Object.toString()", source.determineCacheName(m, null, ""));
	}

	@Test
	public void defaultCacheNameWithParameters() {
		Method m = ReflectionUtils.findMethod(Comparator.class, "compare", Object.class, Object.class);
		assertEquals("java.util.Comparator.compare(java.lang.Object,java.lang.Object)",
				source.determineCacheName(m, null, ""));
	}

	private void assertDefaults(BaseKeyCacheOperation<?> operation) {
		assertEquals(defaultCacheResolver, operation.getCacheResolver());
		assertEquals(defaultKeyGenerator, operation.getKeyGenerator());
	}

	protected <T extends JCacheOperation<?>> T getDefaultCacheOperation(Class<T> operationType, Class<?>... parameterTypes) {
		return getCacheOperation(operationType, AnnotatedJCacheableService.class, name.getMethodName(), parameterTypes);
	}

	protected <T extends JCacheOperation<?>> T getCacheOperation(Class<T> operationType, Class<?> targetType,
																 String methodName, Class<?>... parameterTypes) {
		JCacheOperation<?> result = getCacheOperation(targetType, methodName, parameterTypes);
		assertNotNull(result);
		assertEquals(operationType, result.getClass());
		return operationType.cast(result);
	}

	private JCacheOperation<?> getCacheOperation(Class<?> targetType, String methodName, Class<?>... parameterTypes) {
		Method method = ReflectionUtils.findMethod(targetType, methodName, parameterTypes);
		Assert.notNull(method, "requested method '" + methodName + "'does not exist");
		return source.getCacheOperation(method, targetType);
	}
}
