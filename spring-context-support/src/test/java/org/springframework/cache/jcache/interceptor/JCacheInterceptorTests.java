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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.AbstractJCacheTests;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.ReflectionUtils;

/**
 * @author Stephane Nicoll
 */
public class JCacheInterceptorTests extends AbstractJCacheTests {

	private final Invoker dummyInvoker = new DummyInvoker();

	@Test
	public void severalCachesNotSupported() {
		JCacheInterceptor interceptor = createInterceptor(createOperationSource(
				cacheManager, new TestCacheResolver("default", "exception"),
				defaultExceptionCacheResolver, defaultKeyGenerator));

		AnnotatedJCacheableService service = new AnnotatedJCacheableService(cacheManager.getCache("default"));
		Method m = ReflectionUtils.findMethod(AnnotatedJCacheableService.class, "cache", String.class);

		try {
			interceptor.execute(dummyInvoker, service, m, new Object[]{"myId"});
		} catch (IllegalStateException e) {
			assertTrue(e.getMessage().contains("JSR-107 only supports a single cache."));
		} catch (Throwable t) {
			fail("Unexpected: " + t);
		}
	}

	@Test
	public void noCacheCouldBeResolved() {
		JCacheInterceptor interceptor = createInterceptor(createOperationSource(
				cacheManager, new TestCacheResolver(), // Returns empty list
				defaultExceptionCacheResolver, defaultKeyGenerator));

		AnnotatedJCacheableService service = new AnnotatedJCacheableService(cacheManager.getCache("default"));
		Method m = ReflectionUtils.findMethod(AnnotatedJCacheableService.class, "cache", String.class);

		try {
			interceptor.execute(dummyInvoker, service, m, new Object[]{"myId"});
		} catch (IllegalStateException e) {
			assertTrue(e.getMessage().contains("Cache could not have been resolved for"));
		} catch (Throwable t) {
			fail("Unexpected: " + t);
		}
	}

	@Test
	public void cacheManagerMandatoryIfCacheResolverNotSetSet() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("'cacheManager' is required");
		createOperationSource(null, null, null, defaultKeyGenerator);
	}

	@Test
	public void cacheManagerOptionalIfCacheResolversSet() {
		createOperationSource(null, defaultCacheResolver, defaultExceptionCacheResolver, defaultKeyGenerator);
	}

	protected JCacheOperationSource createOperationSource(CacheManager cacheManager,
														  CacheResolver cacheResolver,
														  CacheResolver exceptionCacheResolver,
														  KeyGenerator keyGenerator) {
		DefaultJCacheOperationSource source = new DefaultJCacheOperationSource();
		source.setApplicationContext(new StaticApplicationContext());
		source.setCacheManager(cacheManager);
		source.setCacheResolver(cacheResolver);
		source.setExceptionCacheResolver(exceptionCacheResolver);
		source.setKeyGenerator(keyGenerator);
		source.afterPropertiesSet();
		return source;
	}


	protected JCacheInterceptor createInterceptor(JCacheOperationSource source) {
		JCacheInterceptor interceptor = new JCacheInterceptor();
		interceptor.setCacheOperationSource(source);
		interceptor.afterPropertiesSet();
		return interceptor;
	}

	private class TestCacheResolver implements CacheResolver {

		private final String[] cacheNames;

		private TestCacheResolver(String... cacheNames) {
			this.cacheNames = cacheNames;
		}

		@Override
		public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
			List<Cache> result = new ArrayList<Cache>();
			for (String cacheName : cacheNames) {
				result.add(cacheManager.getCache(cacheName));
			}
			return result;
		}
	}

	private static class DummyInvoker implements Invoker {

		@Override
		public Object invoke() throws ThrowableWrapper {
			return null;
		}
	}
}
