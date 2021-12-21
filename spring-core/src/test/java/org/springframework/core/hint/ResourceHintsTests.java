/*
 * Copyright 2002-2021 the original author or authors.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.core.hint.PatternResourceHint.Format;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ResourceHints}.
 *
 * @author Stephane Nicoll
 */
class ResourceHintsTests {

	private final ResourceHints resourceHints = new ResourceHints();

	@Test
	void registerType() {
		this.resourceHints.registerType(String.class);
		assertThat(this.resourceHints.patterns()).singleElement()
				.satisfies(patternOf(Format.EXACT_MATCH, "java/lang/String.class"));
	}

	@Test
	void registerTypeSeveralTimesAddsOnlyOneEntry() {
		this.resourceHints.registerType(String.class);
		this.resourceHints.registerType(TypeReference.of(String.class));
		assertThat(this.resourceHints.patterns()).singleElement()
				.satisfies(patternOf(Format.EXACT_MATCH, "java/lang/String.class"));
	}

	@Test
	void registerExactMatch() {
		this.resourceHints.registerExactMatch("com/example/test.properties");
		this.resourceHints.registerExactMatch("com/example/another.properties");
		assertThat(this.resourceHints.patterns())
				.anySatisfy(patternOf(Format.EXACT_MATCH, "com/example/test.properties"))
				.anySatisfy(patternOf(Format.EXACT_MATCH, "com/example/another.properties"))
				.hasSize(2);
	}

	@Test
	void registerPattern() {
		this.resourceHints.registerPattern("com/example/*.properties");
		assertThat(this.resourceHints.patterns()).singleElement()
				.satisfies(patternOf(Format.PATTERN, "com/example/*.properties"));
	}

	@Test
	void registerPatternWithIncludesAndExcludes() {
		this.resourceHints.registerPattern("com/example/*.properties",
				resourceHint -> resourceHint.excudes("com/example/to-ignore.properties"));
		assertThat(this.resourceHints.patterns()).singleElement()
				.satisfies(patternOf(Format.PATTERN,
						List.of("com/example/*.properties"),
						List.of("com/example/to-ignore.properties")));
	}


	private Consumer<PatternResourceHint> patternOf(Format format, String... includes) {
		return patternOf(format, Arrays.asList(includes), Collections.emptyList());
	}

	private Consumer<PatternResourceHint> patternOf(Format format, List<String> includes, List<String> excludes) {
		return pattern -> {
			assertThat(pattern.getFormat()).isEqualTo(format);
			assertThat(pattern.getIncludes()).containsExactlyElementsOf(includes);
			assertThat(pattern.getExcludes()).containsExactlyElementsOf(excludes);
		};
	}

}
