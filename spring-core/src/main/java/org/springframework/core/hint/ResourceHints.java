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

package org.springframework.core.hint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.springframework.core.hint.PatternResourceHint.Builder;
import org.springframework.core.hint.PatternResourceHint.Format;
import org.springframework.lang.Nullable;

/**
 * Gather the need for resources available at runtime.
 *
 * @author Stephane Nicoll
 */
public class ResourceHints {

	private final Set<TypeReference> types = new HashSet<>();

	private final List<Builder> resourceHints = new ArrayList<>();

	private final Set<String> resourceBundleHints = new LinkedHashSet<>();


	public Stream<PatternResourceHint> patterns() {
		return Stream.concat(
				this.types.stream().map(TypeResourceHint::new),
				this.resourceHints.stream().map(Builder::build));
	}

	public Stream<ResourceBundleResourceHint> resourceBundles() {
		return this.resourceBundleHints.stream().map(ResourceBundleResourceHint::new);
	}

	public ResourceHints registerExactMatch(String include, Consumer<Builder> resourceHint) {
		return register(Format.EXACT_MATCH, include, resourceHint);
	}

	public ResourceHints registerExactMatch(String include) {
		return registerExactMatch(include, null);
	}

	public ResourceHints registerPattern(String include, Consumer<Builder> resourceHint) {
		return register(Format.PATTERN, include, resourceHint);
	}

	public ResourceHints registerPattern(String include) {
		return registerPattern(include, null);
	}

	public ResourceHints registerRegexp(String include, Consumer<Builder> resourceHint) {
		return register(Format.REGEXP, include, resourceHint);
	}

	public ResourceHints registerRegexp(String include) {
		return registerRegexp(include, null);
	}

	private ResourceHints register(Format format, String include, @Nullable Consumer<Builder> resourceHint) {
		Builder builder = new Builder(format).includes(include);
		if (resourceHint != null) {
			resourceHint.accept(builder);
		}
		this.resourceHints.add(builder);
		return this;
	}

	public ResourceHints registerType(TypeReference type) {
		this.types.add(type);
		return this;
	}

	public ResourceHints registerType(Class<?> type) {
		return registerType(TypeReference.of(type));
	}

	public ResourceHints registerResourceBundle(String baseName) {
		this.resourceBundleHints.add(baseName);
		return this;
	}

}
