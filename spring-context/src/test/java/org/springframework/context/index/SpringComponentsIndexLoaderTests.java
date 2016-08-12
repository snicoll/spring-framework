/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.index;

import java.util.Set;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link SpringComponentsIndexLoader}.
 *
 * @author Stephane Nicoll
 */
public class SpringComponentsIndexLoaderTests {

	@Test
	public void loadIndexSeveralMatches() {
		SpringComponentsIndex index = SpringComponentsIndexLoader.loadIndex(null);
		Set<String> components = index.getComponents("foo");
		assertThat(components, containsInAnyOrder(
				"org.springframework.context.index.Sample1",
				"org.springframework.context.index.Sample2"));
	}

	@Test
	public void loadIndexSingleMatch() {
		SpringComponentsIndex index = SpringComponentsIndexLoader.loadIndex(null);
		Set<String> components = index.getComponents("biz");
		assertThat(components, containsInAnyOrder(
				"org.springframework.context.index.Sample3"));
	}

	@Test
	public void loadIndexNoMatch() {
		SpringComponentsIndex index = SpringComponentsIndexLoader.loadIndex(null);
		Set<String> components = index.getComponents("none");
		assertThat(components, hasSize(0));
	}

}
