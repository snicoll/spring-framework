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

import javax.cache.annotation.CacheRemove;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.jcache.model.CacheRemoveOperation;

/**
 * Intercept methods annotated with {@link CacheRemove}.
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
@SuppressWarnings("serial")
public class CacheRemoveEntryInterceptor extends AbstractKeyCacheInterceptor<CacheRemoveOperation, CacheRemove> {

	@Override
	protected Object invoke(CacheOperationInvocationContext<CacheRemoveOperation> context,
							Invoker invoker) throws Throwable {
		CacheRemoveOperation operation = context.getOperation();

		final boolean earlyRemove = operation.isEarlyRemove();

		if (earlyRemove) {
			removeValue(context);
		}

		try {
			Object result = invoker.invoke();
			if (!earlyRemove) {
				removeValue(context);
			}
			return result;
		} catch (Invoker.ThrowableWrapper t) {
			Throwable ex = t.original;
			if (!earlyRemove && operation.getExceptionTypeFilter().match(ex.getClass())) {
				removeValue(context);
			}
			throw ex;
		}
	}

	private void removeValue(CacheOperationInvocationContext<CacheRemoveOperation> context) {
		Object key = generateKey(context);
		Cache cache = resolveCache(context);
		if (logger.isTraceEnabled()) {
			logger.trace("Invalidating cache key [" + key + "] for operation " +
					context.getOperation() + " on method " + context.getMethod());
		}
		cache.evict(key);
	}
}
