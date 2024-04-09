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
 * Internal parsing utilities to discover the presence of
 * {@link BeanOverride @BeanOverride} on fields, and create the relevant
 * {@link OverrideMetadata} accordingly.
 *
 * @author Simon Baslé
 * @author Sam Brannen
 * @since 6.2
 */
abstract class BeanOverrideParsingUtils {

	/**
	 * Check if at lease one field of the given {@code clazz} is meta-annotated
	 * with {@link BeanOverride @BeanOverride}.
	 * @param clazz the class which fields to inspect
	 * @return {@code true} if there is a bean override annotation present,
	 * {@code false} otherwise
	 */
	static boolean hasBeanOverride(Class<?> clazz) {
		AtomicBoolean hasBeanOverride = new AtomicBoolean();
		ReflectionUtils.doWithFields(clazz, field -> {
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
	 * Parse the specified classes for the presence of fields annotated with
	 * {@link BeanOverride @BeanOverride}, and create an {@link OverrideMetadata}
	 * for each.
	 * @param classes the classes to parse
	 */
	static Set<OverrideMetadata> parse(Iterable<Class<?>> classes) {
		Set<OverrideMetadata> result = new LinkedHashSet<>();
		classes.forEach(c -> ReflectionUtils.doWithFields(c, field -> parseField(field, c, result)));
		return result;
	}

	/**
	 * Convenience method to {@link #parse(Iterable) parse} a single test class.
	 */
	static Set<OverrideMetadata> parse(Class<?> clazz) {
		return parse(List.of(clazz));
	}

	/**
	 * Determine the {@link ResolvableType} of the field for which an
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
