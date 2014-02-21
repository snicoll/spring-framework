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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResult;

import org.junit.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.jcache.AbstractJCacheTests;
import org.springframework.cache.jcache.model.CacheResultOperation;
import org.springframework.cache.jcache.model.DefaultCacheMethodDetails;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author Stephane Nicoll
 */
public class CacheResolverAdapterTests extends AbstractJCacheTests {

	@Test
	public void resolveSimpleCache() {
		DefaultCacheInvocationContext<?> dummyContext = createDummyContext();
		CacheResolverAdapter adapter = new CacheResolverAdapter(getCacheResolver(dummyContext, "testCache"));
		Collection<? extends Cache> caches = adapter.resolveCaches(dummyContext);
		assertNotNull(caches);
		assertEquals(1, caches.size());
		assertEquals("testCache", caches.iterator().next().getName());
	}


	protected CacheResolver getCacheResolver(CacheInvocationContext<? extends Annotation> context, String cacheName) {
		CacheResolver cacheResolver = mock(CacheResolver.class);
		javax.cache.Cache cache = mock(javax.cache.Cache.class);
		given(cache.getName()).willReturn(cacheName);
		given(cacheResolver.resolveCache(context)).willReturn(cache);
		return cacheResolver;
	}

	protected DefaultCacheInvocationContext<?> createDummyContext() {
		Method method = ReflectionUtils.findMethod(Sample.class, "get", String.class);
		Assert.notNull(method);
		CacheResult cacheAnnotation = method.getAnnotation(CacheResult.class);
		CacheMethodDetails<CacheResult> methodDetails =
				new DefaultCacheMethodDetails<>(method, cacheAnnotation, "test");
		CacheResultOperation operation = new CacheResultOperation(methodDetails,
				defaultCacheResolver, defaultKeyGenerator, defaultExceptionCacheResolver);
		return new DefaultCacheInvocationContext<CacheResult>(operation, new Sample(), new Object[] {"id"});
	}


	static class Sample {

		@CacheResult
		private Object get(String id) {
			return null;
		}
	}

}
