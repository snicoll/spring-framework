/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.cache.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManagerTests;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.junit.Assert.*;

/**
 * @author Stephane Nicoll
 */
public class EhCacheCacheManagerTests extends AbstractTransactionSupportingCacheManagerTests<EhCacheCacheManager> {

	private CacheManager nativeCacheManager;
	private EhCacheCacheManager cacheManager;
	private EhCacheCacheManager transactionalCacheManager;

	@Before
	public void setup() {
		this.nativeCacheManager = new CacheManager(new Configuration().name("EhCacheCacheManagerTests")
				.defaultCache(new CacheConfiguration("default", 100)));
		addNativeCache(CACHE_NAME);

		this.cacheManager = new EhCacheCacheManager(this.nativeCacheManager);
		this.cacheManager.setTransactionAware(false);
		this.cacheManager.afterPropertiesSet();

		this.transactionalCacheManager = new EhCacheCacheManager(this.nativeCacheManager);
		this.transactionalCacheManager.setTransactionAware(true);
		this.transactionalCacheManager.afterPropertiesSet();
	}

	@After
	public void tearDown() {
		this.nativeCacheManager.shutdown();
	}

	@Test
	public void localCacheManagerIsClosed() {
		ConfigurableApplicationContext context =
				new AnnotationConfigApplicationContext(LocalEhCacheConfiguration.class);
		assertNotNull("ehcache manager should have been found",
				CacheManager.getCacheManager("localCacheManager"));
		context.close();
		assertNull("ehcache manager should have been disposed on context shutdown",
				CacheManager.getCacheManager("localCacheManager"));
	}

	@Test
	public void sharedCacheManagerStaysAvailable() {
		ConfigurableApplicationContext context =
				new AnnotationConfigApplicationContext(SharedEhCacheConfiguration.class);
		assertNotNull("ehcache manager should have been found",
				CacheManager.getCacheManager("sharedCacheManager"));
		context.close();
		assertNotNull("ehcache manager should not have been disposed on context shutdown",
				CacheManager.getCacheManager("sharedCacheManager"));
	}


	@Override
	protected EhCacheCacheManager getCacheManager(boolean transactionAware) {
		if (transactionAware) {
			return this.transactionalCacheManager;
		}
		else {
			return this.cacheManager;
		}
	}

	@Override
	protected Class<? extends org.springframework.cache.Cache> getCacheType() {
		return EhCacheCache.class;
	}

	@Override
	protected void addNativeCache(String cacheName) {
		this.nativeCacheManager.addCache(cacheName);
	}

	@Override
	protected void removeNativeCache(String cacheName) {
		this.nativeCacheManager.removeCache(cacheName);
	}


	@org.springframework.context.annotation.Configuration
	static class LocalEhCacheConfiguration {

		@Bean
		public EhCacheCacheManager cacheManager() {
			return EhCacheCacheManager.forLocalCacheManager(
					new CacheManager(new Configuration().name("localCacheManager")
							.defaultCache(new CacheConfiguration("default", 100))));
		}
	}

	@org.springframework.context.annotation.Configuration
	static class SharedEhCacheConfiguration {

		@Bean
		public EhCacheCacheManager cacheManager() {
			return EhCacheCacheManager.forSharedCacheManager(
					new CacheManager(new Configuration().name("sharedCacheManager")
							.defaultCache(new CacheConfiguration("default", 100))));
		}
	}

}
