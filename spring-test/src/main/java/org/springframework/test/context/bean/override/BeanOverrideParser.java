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
import java.util.Collections;
import java.util.LinkedHashSet;
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
 * A parser that discovers annotations meta-annotated with {@link BeanOverride @BeanOverride}
 * on fields of a given class and creates {@link OverrideMetadata} accordingly.
 *
 * @author Simon Baslé
 * @author Sam Brannen
 * @since 6.2
 */
class BeanOverrideParser {

	private final Set<OverrideMetadata> parsedMetadata = new LinkedHashSet<>();

	private final Set<Class<?>> detectedClasses = new LinkedHashSet<>();


	/**
	 * Get the set of {@link OverrideMetadata} once {@link #parse(Class)} has been called.
	 */
	Set<OverrideMetadata> getOverrideMetadata() {
		return Collections.unmodifiableSet(this.parsedMetadata);
	}

	/**
	 * Get the set of {@link Class} in which metadata was detected so far by
	 * previous calls to either {@link #parse(Class)} or {@link #hasBeanOverride(Class)}.
	 */
	Set<Class<?>> getDetectedClasses() {
		return Collections.unmodifiableSet(this.detectedClasses);
	}

	/**
	 * Discover fields of the provided class that are meta-annotated with
	 * {@link BeanOverride @BeanOverride}, then instantiate the corresponding
	 * {@link BeanOverrideProcessor} and use it to create {@link OverrideMetadata}
	 * for each field.
	 * <p>Each call to {@code parse} adds the parsed metadata to the parser's
	 * override metadata {@link #getOverrideMetadata() set}, as well as the
	 * {@link Class} to the {@link #getDetectedClasses() classes set} if any
	 * metadata was created for it.
	 * @param testClass the test class in which to inspect fields
	 */
	void parse(Class<?> testClass) {
		ReflectionUtils.doWithFields(testClass, field -> parseField(field, testClass));
	}

	/**
	 * Check if any field of the provided {@code testClass} is meta-annotated
	 * with {@link BeanOverride @BeanOverride}.
	 * <p>This is similar to the initial discovery of fields in {@link #parse(Class)}
	 * without the heavier steps of instantiating processors and creating
	 * {@link OverrideMetadata}. Consequently, this method leaves the current
	 * state of the parser's override metadata {@link #getOverrideMetadata() set}
	 * unchanged, but adds to the {@link #getDetectedClasses() classes set}
	 * if necessary.
	 * @param testClass the class which fields to inspect
	 * @return true if there is a bean override annotation present, false otherwise
	 * @see #parse(Class)
	 */
	boolean hasBeanOverride(Class<?> testClass) {
		AtomicBoolean hasBeanOverride = new AtomicBoolean();
		ReflectionUtils.doWithFields(testClass, field -> {
			if (hasBeanOverride.get()) {
				return;
			}
			boolean present = MergedAnnotations.from(field, DIRECT).isPresent(BeanOverride.class);
			hasBeanOverride.compareAndSet(false, present);
		});
		if (hasBeanOverride.get()) {
			this.detectedClasses.add(testClass);
			return true;
		}
		return false;
	}

	private void parseField(Field field, Class<?> testClass) {
		AtomicBoolean overrideAnnotationFound = new AtomicBoolean();

		MergedAnnotations.from(field, DIRECT).stream(BeanOverride.class).forEach(mergedAnnotation -> {
			Assert.state(mergedAnnotation.isMetaPresent(), "@BeanOverride annotation must be meta-present");

			BeanOverride beanOverride = mergedAnnotation.synthesize();
			BeanOverrideProcessor processor = BeanUtils.instantiateClass(beanOverride.value());
			MergedAnnotation<?> metaSource = mergedAnnotation.getMetaSource();
			Assert.state(metaSource != null, "Meta-annotation source must not be null");
			Annotation composedAnnotation = metaSource.synthesize();
			ResolvableType typeToOverride = processor.getOrDeduceType(field, composedAnnotation, testClass);

			Assert.state(overrideAnnotationFound.compareAndSet(false, true),
					() -> "Multiple @BeanOverride annotations found on field: " + field);
			OverrideMetadata metadata = processor.createMetadata(field, composedAnnotation, typeToOverride, testClass);
			this.parsedMetadata.add(metadata);
			this.detectedClasses.add(testClass);
		});
	}

}
