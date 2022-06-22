/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.aot.generate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.javapoet.ClassName;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Generate unique class names based on target {@link Class} and a feature
 * name. This class is stateful so the same instance should be used for all
 * name generation.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
class ClassNameGenerator {

	private static final String SEPARATOR = "__";

	private static final String AOT_FEATURE = "Aot";

	private final Map<String, AtomicInteger> sequenceGenerator = new ConcurrentHashMap<>();

	private final Class<?> mainTarget;

	private final String featureNamePrefix;

	public ClassNameGenerator(Class<?> mainTarget, String featureNamePrefix) {
		this.mainTarget = mainTarget;
		this.featureNamePrefix = featureNamePrefix;
	}

	/**
	 * Generate a unique {@link ClassName} based on the specified
	 * {@code featureName} and {@code target}. The class name is
	 * a suffixed version of the target.
	 * <p>For instance, a {@code com.example.Demo} target with an
	 * {@code Initializer} feature name leads to a
	 * {@code com.example.Demo__Initializer} generated class name. If such a
	 * feature was already requested for this target, a counter is used to
	 * ensure uniqueness.
	 * @param target the class the newly generated class relates to, or
	 * {@code null} to use the main target
	 * @param featureName the name of the feature that the generated class
	 * supports
	 * @return a unique generated class name
	 */
	public ClassName generateClassName(@Nullable Class<?> target, String featureName) {
		return generateSequencedClassName(getClassName(target, featureName));
	}

	String getClassName(@Nullable Class<?> target, String featureName) {
		Assert.hasLength(featureName, "'featureName' must not be empty");
		featureName = clean(featureName);
		Class<?> targetToUse = (target != null ? target : this.mainTarget);
		String featureNameToUse = this.featureNamePrefix + featureName;
		return targetToUse.getName().replace("$", "_")
				+ SEPARATOR + StringUtils.capitalize(featureNameToUse);
	}

	private String clean(String name) {
		StringBuilder clean = new StringBuilder();
		boolean lastNotLetter = true;
		for (char ch : name.toCharArray()) {
			if (!Character.isLetter(ch)) {
				lastNotLetter = true;
				continue;
			}
			clean.append(lastNotLetter ? Character.toUpperCase(ch) : ch);
			lastNotLetter = false;
		}
		return (!clean.isEmpty()) ? clean.toString() : AOT_FEATURE;
	}

	private ClassName generateSequencedClassName(String name) {
		name = addSequence(name);
		return ClassName.get(ClassUtils.getPackageName(name),
				ClassUtils.getShortName(name));
	}

	private String addSequence(String name) {
		int sequence = this.sequenceGenerator
				.computeIfAbsent(name, key -> new AtomicInteger()).getAndIncrement();
		return (sequence > 0) ? name + sequence : name;
	}

}
