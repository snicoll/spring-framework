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

package org.springframework.cache.jcache.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.interceptor.BeanFactoryJCacheOperationSourceAdvisor;
import org.springframework.cache.jcache.interceptor.DefaultJCacheOperationSource;
import org.springframework.cache.jcache.interceptor.JCacheInterceptor;
import org.springframework.cache.jcache.interceptor.JCacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;

/**
 * Temporary configuration to enable JSR-107 caching. To be merged with the standard
 * caching mechanism.
 * @author Stephane Nicoll
 */
@Configuration
public class BasicJCacheProxyConfiguration {

	@Autowired
	@Qualifier("cacheManager")
	private CacheManager cacheManager;

	@Autowired(required = false)
	@Qualifier("keyGenerator")
	private KeyGenerator keyGenerator;

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanFactoryJCacheOperationSourceAdvisor cacheAdvisor() {
		BeanFactoryJCacheOperationSourceAdvisor advisor =
				new BeanFactoryJCacheOperationSourceAdvisor();
		advisor.setCacheOperationSource(cacheOperationSource());
		advisor.setAdvice(cacheInterceptor());
		advisor.setOrder(Ordered.LOWEST_PRECEDENCE);
		return advisor;
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public JCacheOperationSource cacheOperationSource() {
		DefaultJCacheOperationSource source = new DefaultJCacheOperationSource();
		source.setCacheManager(cacheManager);
		if (keyGenerator != null) {
			source.setKeyGenerator(keyGenerator);
		}
		return source;
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public JCacheInterceptor cacheInterceptor() {
		JCacheInterceptor interceptor = new JCacheInterceptor();
		interceptor.setCacheOperationSource(cacheOperationSource());
		return interceptor;
	}
}
