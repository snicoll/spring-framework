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

package org.springframework.cache.jcache.config;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.jcache.interceptor.JCacheInterceptor;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author Stephane Nicoll
 */
public abstract class AbstractJCacheCustomInterceptorTests {


	protected ConfigurableApplicationContext ctx;

	protected JCacheableService<?> cs;

	protected Cache exceptionCache;

	/** @return a refreshed application context */
	protected abstract ConfigurableApplicationContext getApplicationContext();

	@Before
	public void setup() {
		ctx = getApplicationContext();
		cs = ctx.getBean("service", JCacheableService.class);
		exceptionCache = ctx.getBean("exceptionCache", Cache.class);
	}

	@After
	public void tearDown() {
		ctx.close();
	}

	@Test
	public void onlyOneInterceptorIsAvailable() {
		Map<String, JCacheInterceptor> interceptors = ctx.getBeansOfType(JCacheInterceptor.class);
		assertEquals("Only one interceptor should be defined", 1, interceptors.size());
		JCacheInterceptor interceptor = interceptors.values().iterator().next();
		assertEquals("Custom interceptor not defined", TestCacheInterceptor.class, interceptor.getClass());
	}

	@Test
	public void customInterceptorAppliesWithRuntimeException() {
		Object o = cs.cacheWithException("id", true);
		assertEquals(55L, o); // See TestCacheInterceptor
	}

	@Test
	public void customInterceptorAppliesWithCheckedException() {
		try {
			cs.cacheWithCheckedException("id", true);
			fail("Should have failed");
		}
		catch (RuntimeException e) {
			assertNotNull("missing original exception", e.getCause());
			assertEquals(IOException.class, e.getCause().getClass());
		}
		catch (Exception e) {
			fail("Wrong exception type " + e);
		}
	}

	/**
	 * A test {@link CacheInterceptor} that handles special exception
	 * types.
	 */
	static class TestCacheInterceptor extends JCacheInterceptor {

		@Override
		protected Object invokeOperation(CacheOperationInvoker invoker) {
			try {
				return super.invokeOperation(invoker);
			}
			catch (CacheOperationInvoker.ThrowableWrapper e) {
				Throwable original = e.getOriginal();
				if (original.getClass() == UnsupportedOperationException.class) {
					return 55L;
				}
				else {
					throw new CacheOperationInvoker.ThrowableWrapper(
							new RuntimeException("wrapping original", original));
				}
			}
		}
	}
}
