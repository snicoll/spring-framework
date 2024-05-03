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

package org.springframework.context.aot;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.context.testfixture.context.aot.scan.noreflective.ReflectiveNotUsed;
import org.springframework.context.testfixture.context.aot.scan.reflective.ReflectiveOnConstructor;
import org.springframework.context.testfixture.context.aot.scan.reflective.ReflectiveOnField;
import org.springframework.context.testfixture.context.aot.scan.reflective.ReflectiveOnInnerField;
import org.springframework.context.testfixture.context.aot.scan.reflective.ReflectiveOnInterface;
import org.springframework.context.testfixture.context.aot.scan.reflective.ReflectiveOnMethod;
import org.springframework.context.testfixture.context.aot.scan.reflective.ReflectiveOnNestedType;
import org.springframework.context.testfixture.context.aot.scan.reflective.ReflectiveOnRecord;
import org.springframework.context.testfixture.context.aot.scan.reflective.ReflectiveOnType;
import org.springframework.context.testfixture.context.aot.scan.reflective2.Reflective2OnType;
import org.springframework.context.testfixture.context.aot.scan.reflective2.reflective21.Reflective21OnType;
import org.springframework.context.testfixture.context.aot.scan.reflective2.reflective22.Reflective22OnType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ReflectiveProcessorAotContributionProvider}.
 *
 * @author Stephane Nicoll
 */
class ReflectiveProcessorAotContributionProviderTests {

	@Test
	void scanWithMatchingCandidates() {
		String packageName = ReflectiveOnType.class.getPackageName();
		BeanFactoryInitializationAotContribution contribution = ReflectiveProcessorAotContributionProvider
				.scan(getClass().getClassLoader(), packageName);
		assertThat(contribution).isNotNull().extracting("classes", InstanceOfAssertFactories.ARRAY)
				.containsOnly(ReflectiveOnType.class, ReflectiveOnInterface.class, ReflectiveOnRecord.class,
						ReflectiveOnField.class, ReflectiveOnConstructor.class, ReflectiveOnMethod.class,
						ReflectiveOnNestedType.Nested.class, ReflectiveOnInnerField.Inner.class);
	}

	@Test
	void scanWithMatchingCandidatesInSubPackages() {
		String packageName = Reflective2OnType.class.getPackageName();
		BeanFactoryInitializationAotContribution contribution = ReflectiveProcessorAotContributionProvider
				.scan(getClass().getClassLoader(), packageName);
		assertThat(contribution).isNotNull().extracting("classes", InstanceOfAssertFactories.ARRAY)
				.containsOnly(Reflective2OnType.class, Reflective21OnType.class, Reflective22OnType.class);
	}

	@Test
	void scanWithNoCandidate() {
		String packageName = ReflectiveNotUsed.class.getPackageName();
		BeanFactoryInitializationAotContribution contribution = ReflectiveProcessorAotContributionProvider
				.scan(getClass().getClassLoader(), packageName);
		assertThat(contribution).isNull();
	}

}
