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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GenerationNamingStrategy}.
 *
 * @author Stephane Nicoll
 */
class GenerationNamingStrategyTests {

	@Test
	void generateComponentClassName() {
		assertThat(createTestNamingStrategy("").forComponent(String.class)
				.withFeatureName("Test").toClassName())
				.hasToString("java.lang.String__Test");
	}

	@Test
	void generateQualifiedComponentClassNameWithDefaultName() {
		assertThat(createTestNamingStrategy("").forComponent(String.class)
				.withQualifiedFeatureName("Test").toClassName())
				.hasToString("java.lang.String__Test");
	}

	@Test
	void generateQualifiedComponentClassNameWithSpecifiedName() {
		assertThat(createTestNamingStrategy("Context").forComponent(String.class)
				.withQualifiedFeatureName("Test").toClassName())
				.hasToString("java.lang.String__ContextTest");
	}

	@Test
	void generateMainTargetClassNameWithDefaultName() {
		assertThat(createTestNamingStrategy("").forMainTarget("Test")
				.toClassName()).hasToString("java.lang.Object__Test");
	}

	@Test
	void generateMainTargetClassNameWithSpecificName() {
		assertThat(createTestNamingStrategy("Context").forMainTarget("Test")
				.toClassName()).hasToString("java.lang.Object__ContextTest");
	}

	public GenerationNamingStrategy createTestNamingStrategy(String featureName) {
		return new GenerationNamingStrategy(Object.class, featureName);
	}

}
