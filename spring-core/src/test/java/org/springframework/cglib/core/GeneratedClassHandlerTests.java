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

package org.springframework.cglib.core;

import java.io.IOException;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link GeneratedClassHandler}.
 *
 * @author Stephane Nicoll
 */
class GeneratedClassHandlerTests {

	private final ClassLoader testClassLoader = getClass().getClassLoader();

	private static final String TEST_CLASS_NAME = "com.example.Test";

	private static final byte[] TEST_CLASS_CONTENT = new byte[] { 'a', 'b' };

	@BeforeEach
	public void clearCache() {
		GeneratedClassHandler.clearAll();
	}

	@Test
	void registerWithNoMatchingPredicateReturnNoOpHandler() throws IOException {
		GeneratedClassHandler handler = mock(GeneratedClassHandler.class);
		GeneratedClassHandler.register(mockPredicate(testClassLoader, false), handler);
		GeneratedClassHandler.register(mockPredicate(testClassLoader, false), handler);
		invokeWithTestClass(GeneratedClassHandler.get(this.testClassLoader));
		verifyNoInteractions(handler);
	}

	@Test
	void registerWithMatchingPredicateInvokeSupplier() throws IOException {
		GeneratedClassHandler handler = mock(GeneratedClassHandler.class);
		Predicate<ClassLoader> predicate = mockPredicate(testClassLoader, true);
		GeneratedClassHandler.register(predicate, handler);
		invokeWithTestClass(GeneratedClassHandler.get(this.testClassLoader));
		verify(handler).generatedClass(TEST_CLASS_NAME, TEST_CLASS_CONTENT);
	}

	@Test
	void registerWithFailingPredicateDoesNotInvokeSupplier() throws IOException {
		GeneratedClassHandler handler = mock(GeneratedClassHandler.class);
		Predicate<ClassLoader> predicate = mockPredicate(testClassLoader, false);
		GeneratedClassHandler.register(predicate, handler);
		invokeWithTestClass(GeneratedClassHandler.get(this.testClassLoader));
		verifyNoInteractions(handler);
	}

	@Test
	@SuppressWarnings("unchecked")
	void registerWithNullClassLoaderIsPossible() throws IOException {
		GeneratedClassHandler handler = mock(GeneratedClassHandler.class);
		Predicate<ClassLoader> predicate = mock(Predicate.class);
		given(predicate.test(null)).willReturn(true);
		GeneratedClassHandler.register(predicate, handler);
		invokeWithTestClass(GeneratedClassHandler.get(null));
		verify(predicate).test(null);
		verify(handler).generatedClass(TEST_CLASS_NAME, TEST_CLASS_CONTENT);
	}

	@Test
	void registerWithMultipleMatchingPredicatesInvokeSuppliers() throws IOException {
		GeneratedClassHandler handler = mock(GeneratedClassHandler.class);
		Predicate<ClassLoader> predicate = mockPredicate(testClassLoader, true);
		GeneratedClassHandler.register(predicate, handler);
		GeneratedClassHandler handler2 = mock(GeneratedClassHandler.class);
		Predicate<ClassLoader> predicate2 = mockPredicate(testClassLoader, true);
		GeneratedClassHandler.register(predicate2, handler2);
		invokeWithTestClass(GeneratedClassHandler.get(this.testClassLoader));
		verify(handler).generatedClass(TEST_CLASS_NAME, TEST_CLASS_CONTENT);
		verify(handler2).generatedClass(TEST_CLASS_NAME, TEST_CLASS_CONTENT);
	}

	@Test
	void clearPredicateDoesNotInvokeIt() throws IOException {
		GeneratedClassHandler handler = mock(GeneratedClassHandler.class);
		Predicate<ClassLoader> predicate = mockPredicate(testClassLoader, true);
		GeneratedClassHandler.register(predicate, handler);
		invokeWithTestClass(GeneratedClassHandler.get(this.testClassLoader));
		verify(predicate).test(testClassLoader);
		verify(handler).generatedClass(TEST_CLASS_NAME, TEST_CLASS_CONTENT);
		GeneratedClassHandler.clear(predicate);
		invokeWithTestClass(GeneratedClassHandler.get(this.testClassLoader));
		verifyNoMoreInteractions(predicate, handler);
	}

	private void invokeWithTestClass(GeneratedClassHandler handler) throws IOException {
		assertThat(handler).isNotNull();
		handler.generatedClass(TEST_CLASS_NAME, TEST_CLASS_CONTENT);
	}

	@SuppressWarnings("unchecked")
	private Predicate<ClassLoader> mockPredicate(ClassLoader classLoader, boolean outcome) {
		Predicate<ClassLoader> mock = mock(Predicate.class);
		given(mock.test(classLoader)).willReturn(outcome);
		return mock;
	}

}
