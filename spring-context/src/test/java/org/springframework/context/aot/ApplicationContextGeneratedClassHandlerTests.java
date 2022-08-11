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

package org.springframework.context.aot;

import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.testfixture.aot.generate.TestGenerationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ApplicationContextGeneratedClassHandler}.
 *
 * @author Stephane Nicoll
 */
class ApplicationContextGeneratedClassHandlerTests {

	private static final byte[] TEST_CONTENT = new byte[] { 'a' };

	private final TestGenerationContext generationContext;

	private final ApplicationContextGeneratedClassHandler handler;

	public ApplicationContextGeneratedClassHandlerTests() {
		this.generationContext = new TestGenerationContext();
		this.handler = new ApplicationContextGeneratedClassHandler(this.generationContext);
	}

	@Test
	void handlerGenerateRuntimeHints() {
		String className = "com.example.Test$$Proxy$$1";
		this.handler.generatedClass(className, TEST_CONTENT);
		assertThat(RuntimeHintsPredicates.reflection().onType(TypeReference.of(className))
				.withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS))
				.accepts(this.generationContext.getRuntimeHints());
	}

}
