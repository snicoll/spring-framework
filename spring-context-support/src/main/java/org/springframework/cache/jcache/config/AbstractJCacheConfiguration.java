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

import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Configuration;

/**
 * Abstract JSR-107 specific {@code @Configuration} class providing common
 * structure for enabling JSR-107 annotation-driven cache management capability.
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see JCacheConfigurer
 */
@Configuration
public class AbstractJCacheConfiguration extends AbstractCachingConfiguration<JCacheConfigurer> {

	protected CacheResolver cacheResolver;
	protected CacheResolver exceptionCacheResolver;

	@Override
	protected void useCachingConfigurer(JCacheConfigurer config) {
		super.useCachingConfigurer(config);
		this.cacheResolver = config.cacheResolver();
		this.exceptionCacheResolver = config.exceptionCacheResolver();
	}
}
