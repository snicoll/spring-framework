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

package org.springframework.aot.hint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.aot.hint.MemberCategory;

/**
 * Register reflection hints against an arbitrary number of target classes.
 *
 * <p>When using this annotation directly, only the defined
 * {@linkplain #memberCategories() member categories} are registered for each
 * target class. The target classes can be specified by class or class names.
 * When both are specified, they are all considered. If no target class is
 * specified, the current class is used.
 *
 * <p>This annotation can be used as a meta-annotation to customize how hints
 * are registered against each target class.
 *
 * <p>The annotated element can be any type that is target for registration:
 * <pre class="code">
 * &#064;Configuration
 * &#064;RegisterReflection(classes = CustomerEntry.class, memberCategories = PUBLIC_FIELDS)
 * public class MyConfig {
 *     // ...
 * }</pre>
 *
 * TODO: complete
 * @author Stephane Nicoll
 * @since 6.2
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(RegisterReflections.class)
@Reflective(RegisterReflectionReflectiveProcessor.class)
public @interface RegisterReflection {

	/**
	 * Classes for which reflection hints should be registered. Consider using
	 * {@link #classNames()} for classes that are not public in the current
	 * scope. If both {@code classes} and {@code classNames} are specified, they
	 * are merged in a single set.
	 * <p>
	 * By default, the annotated type is the target of the registration. When
	 * placed on a method, at least one class must be specified.
	 * @see #classNames()
	 */
	Class<?>[] classes() default {};

	/**
	 * Alternative to {@link #classes()} to specify the classes as class names.
	 * @see #classes()
	 */
	String[] classNames() default {};

	/**
	 * Specify the {@linkplain MemberCategory member categories} to enable.
	 */
	MemberCategory[] memberCategories() default {};

}
