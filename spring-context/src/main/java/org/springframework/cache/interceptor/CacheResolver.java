package org.springframework.cache.interceptor;

import java.util.Collection;

import org.springframework.cache.Cache;

/**
 * Determine the {@link Cache} instance(s) to use for an intercepted method invocation.
 * <p>Implementations MUST be thread-safe.
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
public interface CacheResolver {

	/**
	 * Return the cache(s) to use for the specified invocation.
	 *
	 * @param context the context of the particular invocation
	 * @return the cache(s) to use (never null)
	 */
	Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context);
}
