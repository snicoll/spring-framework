/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @TransactionConfig} provides a mechanism for sharing common
 * transaction-related settings at the class level.
 *
 * <p>When this annotation is present on a given class, it provides a set
 * of default settings for any transaction operation defined in that class.
 *
 * @author Stephane Nicoll
 * @since 6.2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransactionConfig {

	/**
	 * A <em>qualifier</em> value for transactional operations defined in the
	 * annotated class.
	 * <p>If none is set at the operation level, this is used instead of the default.
	 * <p>May be used to determine the target transaction manager, matching the
	 * qualifier value (or the bean name) of a specific
	 * {@link org.springframework.transaction.TransactionManager TransactionManager}
	 * bean definition.
	 * @see Transactional#transactionManager()
	 * @see org.springframework.transaction.ReactiveTransactionManager
	 */
	String transactionManager() default "";

	/**
	 * Defines zero (0) or more exception {@linkplain Class types}, which must be
	 * subclasses of {@link Throwable}, indicating which exception types must cause
	 * a transaction rollback for transactional operations defined in the annotated
	 * class.
	 * <p>If none is set at the operation level, these are used instead of the default.
	 * @see Transactional#rollbackFor()
	 * @see #rollbackForClassName
	 */
	Class<? extends Throwable>[] rollbackFor() default {};

	/**
	 * Defines zero (0) or more exception name patterns (for exceptions which must be a
	 * subclass of {@link Throwable}), indicating which exception types must cause
	 * a transaction rollback for transactional operations defined in the annotated
	 * class.
	 * <p>If none is set at the operation level, these are used instead of the default.
	 * @see Transactional#rollbackForClassName()
	 * @see #rollbackFor
	 */
	String[] rollbackForClassName() default {};

	/**
	 * Defines zero (0) or more exception {@link Class types}, which must be
	 * subclasses of {@link Throwable}, indicating which exception types must
	 * <b>not</b> cause a transaction rollback for transactional operations
	 * defined in the annotated class.
	 * <p>If none is set at the operation level, these are used instead of the default.
	 * @see Transactional#noRollbackFor()
	 * @see #noRollbackForClassName
	 */
	Class<? extends Throwable>[] noRollbackFor() default {};

	/**
	 * Defines zero (0) or more exception name patterns (for exceptions which must be a
	 * subclass of {@link Throwable}) indicating which exception types must <b>not</b>
	 * cause a transaction rollback for transactional operations defined in the annotated
	 * class.
	 * <p>If none is set at the operation level, these are used instead of the default.
	 * @see Transactional#noRollbackForClassName()
	 * @see #noRollbackFor
	 */
	String[] noRollbackForClassName() default {};

}
