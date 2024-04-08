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

package org.springframework.test.context.bean.override;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy.DIRECT;

/**
 * Internal parsing utilities that can be used to discover annotations
 * meta-annotated with {@link BeanOverride @BeanOverride} on fields of a given
 * set of classes and to create {@link OverrideMetadata} accordingly.
 *
 * @author Simon Baslé
 * @author Sam Brannen
 * @since 6.2
 */
final class BeanOverrideParsingUtils {

	private BeanOverrideParsingUtils() {
	}

	/**
	 * Discover fields of the provided classes that are meta-annotated with
	 * {@link BeanOverride @BeanOverride}, then instantiate the corresponding
	 * {@link BeanOverrideProcessor} and use it to create {@link OverrideMetadata}
	 * for each field. The complete set of {@link OverrideMetadata} is returned.
	 * @param testClasses the test classes in which to inspect fields
	 */
	static Set<OverrideMetadata> parse(Iterable<Class<?>> testClasses) {
		Set<OverrideMetadata> result = new LinkedHashSet<>();
		testClasses.forEach(c -> ReflectionUtils.doWithFields(c, field -> parseField(field, c, result)));
		return result;
	}

	/**
	 * Convenience method to {@link #parse(Iterable) parse} a single test class.
	 */
	static Set<OverrideMetadata> parse(Class<?> singleTestClass) {
		return parse(List.of(singleTestClass));
	}

	/**
	 * Check if any field of the provided {@code testClass} is meta-annotated
	 * with {@link BeanOverride @BeanOverride}.
	 * <p>This is similar to the initial discovery of fields in
	 * {@link #parse(Iterable)})} without the heavier steps of instantiating
	 * processors and creating {@link OverrideMetadata}.
	 * @param testClass the class which fields to inspect
	 * @return true if there is a bean override annotation present, false otherwise
	 * @see #parse(Iterable)
	 */
	static boolean hasBeanOverride(Class<?> testClass) {
		AtomicBoolean hasBeanOverride = new AtomicBoolean();
		ReflectionUtils.doWithFields(testClass, field -> {
			if (hasBeanOverride.get()) {
				return;
			}
			boolean present = MergedAnnotations.from(field, DIRECT).isPresent(BeanOverride.class);
			hasBeanOverride.compareAndSet(false, present);
		});
		if (hasBeanOverride.get()) {
			return true;
		}
		return false;
	}

	/**
	 * Determine the field's {@link ResolvableType} for which an
	 * {@link OverrideMetadata} instance will be created, additionally using
	 * the source class if the field type is a generic type.
	 */
	static ResolvableType getResolvableType(Field field, Class<?> testClass) {
		return (field.getGenericType() instanceof TypeVariable ?
				ResolvableType.forField(field, testClass) : ResolvableType.forField(field));
	}

	private static void parseField(Field field, Class<?> testClass, Set<OverrideMetadata> metadataSet) {
		AtomicBoolean overrideAnnotationFound = new AtomicBoolean();

		MergedAnnotations.from(field, DIRECT).stream(BeanOverride.class).forEach(mergedAnnotation -> {
			Assert.state(mergedAnnotation.isMetaPresent(), "@BeanOverride annotation must be meta-present");

			BeanOverride beanOverride = mergedAnnotation.synthesize();
			BeanOverrideProcessor processor = BeanUtils.instantiateClass(beanOverride.value());
			MergedAnnotation<?> metaSource = mergedAnnotation.getMetaSource();
			Assert.state(metaSource != null, "Meta-annotation source must not be null");
			Annotation composedAnnotation = metaSource.synthesize();
			ResolvableType typeToOverride = getResolvableType(field, testClass);

			Assert.state(overrideAnnotationFound.compareAndSet(false, true),
					() -> "Multiple @BeanOverride annotations found on field: " + field);
			OverrideMetadata metadata = processor.createMetadata(field, composedAnnotation, typeToOverride, testClass);
			metadataSet.add(metadata);
		});
	}

}
