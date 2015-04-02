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

package org.springframework.cache.jcache;

import java.util.ArrayList;
import java.util.List;
import javax.cache.Cache;
import javax.cache.CacheManager;

import org.junit.Before;
import org.junit.Test;

import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManagerTests;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.BDDMockito.*;

/**
 * @author Stephane Nicoll
 */
public class JCacheCacheManagerTests extends AbstractTransactionSupportingCacheManagerTests<JCacheCacheManager> {

	private CacheManagerMock cacheManagerMock;
	private JCacheCacheManager cacheManager;
	private JCacheCacheManager transactionalCacheManager;

	@Before
	public void setupOnce() {
		this.cacheManagerMock = new CacheManagerMock();
		this.cacheManagerMock.addCache(CACHE_NAME);

		this.cacheManager = new JCacheCacheManager(this.cacheManagerMock.getCacheManager());
		this.cacheManager.setTransactionAware(false);
		this.cacheManager.afterPropertiesSet();

		this.transactionalCacheManager = new JCacheCacheManager(this.cacheManagerMock.getCacheManager());
		this.transactionalCacheManager.setTransactionAware(true);
		this.transactionalCacheManager.afterPropertiesSet();
	}

	@Test
	public void localCacheManagerIsClosed() {
		ConfigurableApplicationContext context =
				new AnnotationConfigApplicationContext(LocalJCacheConfiguration.class);
		CacheManager jCacheManager = context.getBean(CacheManagerMock.class).getCacheManager();
		verify(jCacheManager, never()).close();
		context.close();
		verify(jCacheManager, times(1)).close();

	}

	@Test
	public void sharedCacheManagerStaysAvailable() {
		ConfigurableApplicationContext context =
				new AnnotationConfigApplicationContext(SharedJCacheConfiguration.class);
		CacheManager jCacheManager = context.getBean(CacheManagerMock.class).getCacheManager();
		verify(jCacheManager, never()).close();
		context.close();
		verify(jCacheManager, never()).close();
	}


	@Override
	protected JCacheCacheManager getCacheManager(boolean transactionAware) {
		if (transactionAware) {
			return this.transactionalCacheManager;
		}
		else {
			return this.cacheManager;
		}
	}

	@Override
	protected Class<? extends org.springframework.cache.Cache> getCacheType() {
		return JCacheCache.class;
	}

	@Override
	protected void addNativeCache(String cacheName) {
		this.cacheManagerMock.addCache(cacheName);
	}

	@Override
	protected void removeNativeCache(String cacheName) {
		this.cacheManagerMock.removeCache(cacheName);
	}


	@Configuration
	static class LocalJCacheConfiguration {

		@Bean
		public JCacheCacheManager cacheManager(CacheManagerMock mock) {
			return JCacheCacheManager.forLocalCacheManager(mock.getCacheManager());
		}

		@Bean
		public CacheManagerMock cacheManagerMock() {
			return new CacheManagerMock();
		}
	}

	@Configuration
	static class SharedJCacheConfiguration {

		@Bean
		public JCacheCacheManager cacheManager(CacheManagerMock mock) {
			return JCacheCacheManager.forSharedCacheManager(mock.getCacheManager());
		}

		@Bean
		public CacheManagerMock cacheManagerMock() {
			return new CacheManagerMock();
		}
	}

	private static class CacheManagerMock {

		private final List<String> cacheNames;
		private final CacheManager cacheManager;

		private CacheManagerMock() {
			this.cacheNames = new ArrayList<String>();
			this.cacheManager = mock(CacheManager.class);
			given(this.cacheManager.getCacheNames()).willReturn(this.cacheNames);
		}

		private CacheManager getCacheManager() {
			return cacheManager;
		}

		@SuppressWarnings("unchecked")
		public void addCache(String name) {
			this.cacheNames.add(name);
			Cache cache = mock(Cache.class);
			given(cache.getName()).willReturn(name);
			given(this.cacheManager.getCache(name)).willReturn(cache);
		}

		public void removeCache(String name) {
			this.cacheNames.remove(name);
			given(this.cacheManager.getCache(name)).willReturn(null);
		}
	}
}
