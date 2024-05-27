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

package org.springframework.test.web.servlet.assertj;

import org.assertj.core.api.AssertProvider;
import org.assertj.core.api.Assertions;

import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * An extension of {@link MockHttpServletRequestBuilder} that supports AssertJ.
 *
 * @author Stephane Nicoll
 * @since 6.2
 */
public final class MockMvcRequestBuilder extends MockHttpServletRequestBuilder<MockMvcRequestBuilder>
		implements AssertProvider<MvcTestResultAssert> {

	private final DefaultMockMvcTester mvcTester;

	MockMvcRequestBuilder(DefaultMockMvcTester mvcTester, HttpMethod httpMethod) {
		super(httpMethod);
		this.mvcTester = mvcTester;
	}

	@Override
	public MvcTestResultAssert assertThat() {
		return Assertions.assertThat(this.mvcTester.perform(this));
	}

}
