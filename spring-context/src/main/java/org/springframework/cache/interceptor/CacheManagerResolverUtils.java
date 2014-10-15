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

package org.springframework.cache.interceptor;

import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author Stephane Nicoll
 * @since 4.1.2
 */
public abstract class CacheManagerResolverUtils {

	/**
	 * Resolve the default {@link CacheManager} from the specified {@link ApplicationContext}.
	 */
	public static CacheManager resolve(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "The application context was not injected as it should.");
		Map<String, CacheManager> cacheManagerBeans = applicationContext.getBeansOfType(CacheManager.class);
		if (!CollectionUtils.isEmpty(cacheManagerBeans)) {
			int nManagers = cacheManagerBeans.size();
			if (nManagers > 1) {
				throw new IllegalStateException(nManagers + " beans of type CacheManager " +
						"were found when only 1 was expected. Remove all but one of the " +
						"CacheManager bean definitions, or implement CachingConfigurer " +
						"to make explicit which CacheManager should be used for " +
						"annotation-driven cache management.");
			}
			return cacheManagerBeans.values().iterator().next();
		}
		else {
			throw new IllegalStateException("No bean of type CacheManager could be found. " +
					"Register a CacheManager bean or remove the @EnableCaching annotation " +
					"from your configuration.");
		}
	}

}
