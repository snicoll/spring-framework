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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resource hint with includes and excludes patterns that match a given
 * {@link Format}.
 *
 * @author Stephane Nicoll
 */
public class PatternResourceHint {

	private final Format format;

	private final List<String> includes;

	private final List<String> excludes;

	protected PatternResourceHint(Format format, List<String> includes, List<String> excludes) {
		this.format = format;
		this.includes = includes;
		this.excludes = excludes;
	}

	private PatternResourceHint(Builder builder) {
		this.format = builder.format;
		this.includes = new ArrayList<>(builder.includes);
		this.excludes = new ArrayList<>(builder.excludes);
	}

	public Format getFormat() {
		return this.format;
	}

	public List<String> getIncludes() {
		return this.includes;
	}

	public List<String> getExcludes() {
		return this.excludes;
	}

	public enum Format {

		/**
		 * A pattern that represents a single file on the classpath, identified by its
		 * exact resource.
		 */
		EXACT_MATCH,

		/**
		 * A pattern that optionally uses {@literal *} and {@literal ?} symbol.
		 */
		PATTERN,

		/**
		 * A pattern that represents a regular expression against the classpath.
		 */
		REGEXP;

	}

	public static class Builder {

		private final Format format;

		private final Set<String> includes = new LinkedHashSet<>();

		private final Set<String> excludes = new LinkedHashSet<>();

		public Builder(Format format) {
			this.format = format;
		}

		public Builder includes(String... includes) {
			this.includes.addAll(Arrays.asList(includes));
			return this;
		}

		public Builder excudes(String... excludes) {
			this.excludes.addAll(Arrays.asList(excludes));
			return this;
		}

		public PatternResourceHint build() {
			return new PatternResourceHint(this);
		}

	}
}
