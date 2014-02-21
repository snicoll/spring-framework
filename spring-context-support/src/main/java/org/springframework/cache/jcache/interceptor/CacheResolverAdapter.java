package org.springframework.cache.jcache.interceptor;


import javax.cache.annotation.CacheInvocationContext;
import java.util.Collection;
import java.util.Collections;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.jcache.JCacheCache;

/**
 * Spring's {@link CacheResolver} implementation that delegates to a standard
 * JSR-107 {@link javax.cache.annotation.CacheResolver}.
 * <p>Used internally to invoke user-based cache resolvers.
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
public class CacheResolverAdapter implements CacheResolver {

	private final javax.cache.annotation.CacheResolver target;

	/**
	 * Create a new instance with the JSR-107 cache resolver to invoke.
	 */
	public CacheResolverAdapter(javax.cache.annotation.CacheResolver target) {
		this.target = target;
	}

	@Override
	public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
		if (!(context instanceof CacheInvocationContext<?>)) {
			throw new IllegalStateException("Unexpected context " + context);
		}
		CacheInvocationContext<?> cacheInvocationContext = (CacheInvocationContext<?>) context;
		javax.cache.Cache<Object, Object> cache = target.resolveCache(cacheInvocationContext);
		if (cache == null) {
			return null;
		}
		return Collections.singleton(new JCacheCache(cache));
	}
}
