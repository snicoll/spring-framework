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

import java.util.Map;
import java.util.stream.StreamSupport;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.util.Assert;

/**
 * The default {@link MockMvcTester} implementation.
 *
 * @author Stephane Nicoll
 * @author Brian Clozel
 * @since 6.2
 */
final class DefaultMockMvcTester implements MockMvcTester {

	private static final MediaType JSON = MediaType.APPLICATION_JSON;

	private final MockMvc mockMvc;

	@Nullable
	private final GenericHttpMessageConverter<Object> jsonMessageConverter;


	DefaultMockMvcTester(MockMvc mockMvc, @Nullable GenericHttpMessageConverter<Object> jsonMessageConverter) {
		Assert.notNull(mockMvc, "mockMVC should not be null");
		this.mockMvc = mockMvc;
		this.jsonMessageConverter = jsonMessageConverter;
	}

	@Override
	public MockMvcRequestBuilder get() {
		return method(HttpMethod.GET);
	}

	@Override
	public MockMvcRequestBuilder head() {
		return method(HttpMethod.HEAD);
	}

	@Override
	public MockMvcRequestBuilder post() {
		return method(HttpMethod.POST);
	}

	@Override
	public MockMvcRequestBuilder put() {
		return method(HttpMethod.PUT);
	}

	@Override
	public MockMvcRequestBuilder patch() {
		return method(HttpMethod.PATCH);
	}

	@Override
	public MockMvcRequestBuilder delete() {
		return method(HttpMethod.DELETE);
	}

	@Override
	public MockMvcRequestBuilder options() {
		return method(HttpMethod.OPTIONS);
	}

	@Override
	public MockMvcRequestBuilder method(HttpMethod method) {
		return new MockMvcRequestBuilder(this, method);
	}

	@Override
	public MvcTestResult perform(RequestBuilder requestBuilder) {
		Object result = getMvcResultOrFailure(requestBuilder);
		if (result instanceof MvcResult mvcResult) {
			return new DefaultMvcTestResult(mvcResult, null, this.jsonMessageConverter);
		}
		else {
			return new DefaultMvcTestResult(null, (Exception) result, this.jsonMessageConverter);
		}
	}

	@Override
	public DefaultMockMvcTester withHttpMessageConverters(Iterable<HttpMessageConverter<?>> httpMessageConverters) {
		return new DefaultMockMvcTester(this.mockMvc, findJsonMessageConverter(httpMessageConverters));
	}

	private Object getMvcResultOrFailure(RequestBuilder requestBuilder) {
		try {
			return this.mockMvc.perform(requestBuilder).andReturn();
		}
		catch (Exception ex) {
			return ex;
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private GenericHttpMessageConverter<Object> findJsonMessageConverter(
			Iterable<HttpMessageConverter<?>> messageConverters) {

		return StreamSupport.stream(messageConverters.spliterator(), false)
				.filter(GenericHttpMessageConverter.class::isInstance)
				.map(GenericHttpMessageConverter.class::cast)
				.filter(converter -> converter.canWrite(null, Map.class, JSON))
				.filter(converter -> converter.canRead(Map.class, JSON))
				.findFirst().orElse(null);
	}

}
