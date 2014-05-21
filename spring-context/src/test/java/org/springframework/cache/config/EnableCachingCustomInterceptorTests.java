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

package org.springframework.cache.config;

import static org.mockito.Mockito.mock;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.CacheTestUtils;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Stephane Nicoll
 */
public class EnableCachingCustomInterceptorTests extends AbstractCustomInterceptorTests {

	@Override
	protected ConfigurableApplicationContext getApplicationContext() {
		return new AnnotationConfigApplicationContext(EnableCachingConfig.class);
	}


	@Configuration
	@EnableCaching
	static class EnableCachingConfig {

		@Bean
		public CacheManager cacheManager() {
			return CacheTestUtils.createSimpleCacheManager("default", "primary", "secondary");
		}

		@Bean
		public CacheableService<?> service() {
			return new DefaultCacheableService();
		}

		@Bean(name = AnnotationConfigUtils.CACHE_INTERCEPTOR_BEAN_NAME)
		public CacheInterceptor cacheInterceptor(
				@Qualifier(AnnotationConfigUtils.CACHE_OPERATION_SOURCE_BEAN_NAME)CacheOperationSource cacheOperationSource) {
			CacheInterceptor cacheInterceptor = new TestCacheInterceptor();
			cacheInterceptor.setCacheManager(cacheManager());
			cacheInterceptor.setCacheOperationSources(cacheOperationSource);
			return cacheInterceptor;
		}

		@Bean // Make sure it's not used and the right one is injected
		public CacheOperationSource dummyCacheOperationSource() {
			 return mock(CacheOperationSource.class);
		}
	}


}
