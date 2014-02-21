package org.springframework.cache.interceptor;

import java.lang.reflect.Method;

/**
 * Represent the context of the invocation of a cache operation.
 * <p>The cache operation is static and independent of a particular invocation,
 * this gathers the operation and a particular invocation.
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
public interface CacheOperationInvocationContext<O extends BasicCacheOperation> {

	/**
	 * Return the cache operation
	 */
	O getOperation();

	/**
	 * Return the target instance on which the method was invoked
	 */
	Object getTarget();

	/**
	 * Return the method
	 */
	Method getMethod();

	/**
	 * Return the argument used to invoke the method
	 */
	Object[] getArgs();
}
