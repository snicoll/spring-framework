/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cache;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import reactor.core.publisher.Mono;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.tests.TestSubscriber;

import java.util.Optional;


/**
 * Tests to check RxJava support.
 *
 * @author Pablo Diaz-Lopez
 */
public class ReactorTests {
	private AnnotationConfigApplicationContext context;
	private TestService bean;

	@Before
	public void before() {
		context = new AnnotationConfigApplicationContext(BasicTestConfig.class);
		bean = context.getBean(TestService.class);
	}

	@Test
	public void single() {
		Cache cache = context.getBean(CacheManager.class).getCache("single");
		Mono<Object> single = bean.single();
		TestSubscriber<Object> testSubscriber = TestSubscriber.create();

		single.subscribe(testSubscriber);
		testSubscriber.await();

		Object cachedValues = cache.get(SimpleKey.EMPTY).get();

		assertTrue("Expected to cache value not wrapper", cachedValues instanceof TestBean);
		testSubscriber.assertValues(cachedValues);
	}

	@Test
	public void empty() {
		Cache cache = context.getBean(CacheManager.class).getCache("empty");
		Mono<Object> empty = bean.empty();
		TestSubscriber<Object> testSubscriber = TestSubscriber.create();

		empty.subscribe(testSubscriber);
		testSubscriber.await();

		Object cachedValues = cache.get(SimpleKey.EMPTY).get();

		assertNull("Null should be cached", cachedValues);
		testSubscriber.assertNoValues();
	}

	@Test
	public void nullValue() {
		Cache cache = context.getBean(CacheManager.class).getCache("nullValue");
		Mono<Object> nullValue = bean.nullValue();
		TestSubscriber<Object> testSubscriber = TestSubscriber.create();

		nullValue.subscribe(testSubscriber);
		testSubscriber.await();

		// Mono values cannot be null
		testSubscriber.assertError();
		assertNull("Shouldn't be cached", cache.get(SimpleKey.EMPTY));
	}

	@Test
	public void throwable() {
		Cache cache = context.getBean(CacheManager.class).getCache("throwable");
		Mono<Object> throwable = bean.nullValue();
		TestSubscriber<Object> testSubscriber = TestSubscriber.create();

		throwable.subscribe(testSubscriber);
		testSubscriber.await();

		Cache.ValueWrapper valueWrapper = cache.get(SimpleKey.EMPTY);

		assertNull("No value should be cached", valueWrapper);

		testSubscriber.assertError();
	}

	@Test(timeout = 1000L)
	public void neverFinish() {
		Cache cache = context.getBean(CacheManager.class).getCache("neverFinish");
		bean.neverFinish();

		Cache.ValueWrapper valueWrapper = cache.get(SimpleKey.EMPTY);

		// No value is cached because nobody subscribed to Mono
		assertNull(valueWrapper);
	}

	@Test
	public void sync() {
		Cache cache = context.getBean(CacheManager.class).getCache("sync");
		Object obj = bean.sync();

		Cache.ValueWrapper valueWrapper = cache.get(SimpleKey.EMPTY);

		assertSame(obj, valueWrapper.get());
	}

	@Test
	@Ignore
	public void monoOptionalFilled() {
		Mono<Optional<Object>> optionalMono1 = bean.monoOptionalFilled();
		Optional<Object> optional1 = optionalMono1.block();

		Mono<Optional<Object>> optionalMono2 = bean.monoOptionalFilled();
		Optional<Object> optional2 = optionalMono1.block();

		assertNotSame(optionalMono1, optionalMono2);
		assertNotSame(optional1, optional2);

		assertSame(optional1.get(), optional2.get());
	}

	@Test
	@Ignore
	public void monoOptionalEmpty() {
		Mono<Optional<Object>> optionalMono1 = bean.monoOptionalEmpty();
		Optional<Object> optional1 = optionalMono1.block();

		Mono<Optional<Object>> optionalMono2 = bean.monoOptionalEmpty();
		Optional<Object> optional2 = optionalMono1.block();

		assertNotSame(optionalMono1, optionalMono2);
		assertNotSame(optional1, optional2);

		assertFalse(optional1.isPresent());
		assertFalse(optional2.isPresent());
	}

	@Test
	public void syncHitsCache() {
		assertSame(bean.sync(), bean.sync());
	}

	@Test
	public void doestBlockToCache() {
		Mono<Object> res1 = bean.single();
		Mono<Object> res2 = bean.single();

		assertNotSame(res1, res2);
		assertNotSame(res1.block(), res2.block());
	}

	@Test
	public void itCachesWhenEmits() {
		Mono<Object> res1 = bean.single();
		Object val1 = res1.block();

		Mono<Object> res2 = bean.single();
		Object val2 = res2.block();

		assertNotSame(res1, res2);
		assertSame(val1, val2);
	}

	@Configuration
	@EnableCaching
	public static class BasicTestConfig {

		@Bean
		public CacheManager cacheManager() {
			return new ConcurrentMapCacheManager();
		}

		@Bean
		public TestService service() {
			return new TestServiceImpl();
		}
	}


	public interface TestService {
		Mono<Object> single();

		Mono<Object> empty();

		Mono<Object> nullValue();

		Mono<Object> throwable();

		Mono<Object> neverFinish();

		Object sync();

		@Cacheable("createMono")
		Mono<Object> createMono(Object value);

		@Cacheable("monoOptionalFilled")
		Mono<Optional<Object>> monoOptionalFilled();

		@Cacheable("monoOptionalEmpty")
		Mono<Optional<Object>> monoOptionalEmpty();
	}


	public static class TestServiceImpl implements TestService {
		@Cacheable("single")
		@Override
		public Mono<Object> single() {
			return createMono(new TestBean(1));
		}

		@Cacheable("empty")
		@Override
		public Mono<Object> empty() {
			return Mono.empty();
		}

		@Cacheable("nullValue")
		@Override
		public Mono<Object> nullValue() {
			return createMono(null);
		}

		@Cacheable("throwable")
		@Override
		public Mono<Object> throwable() {
			return Mono.error(new RuntimeException());
		}

		@Cacheable("neverFinish")
		@Override
		public Mono<Object> neverFinish() {
			return Mono.never();
		}

		@Cacheable("sync")
		@Override
		public Object sync() {
			return new TestBean(2);
		}

		@Cacheable("createMono")
		@Override
		public Mono<Object> createMono(Object value) {
			return Mono.fromCallable(() -> value);
		}

		@Cacheable("monoOptionalFilled")
		@Override
		public Mono<Optional<Object>> monoOptionalFilled() {
			return Mono.fromCallable(() -> Optional.of(new TestBean(3)));
		}

		@Cacheable("monoOptionalEmpty")
		@Override
		public Mono<Optional<Object>> monoOptionalEmpty() {
			return Mono.fromCallable(Optional::empty);
		}
	}


	static class TestBean {
		private Integer value;

		public TestBean(Integer value) {
			this.value = value;
		}
	}
}
