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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.MultiValueMap;

/**
 * Global spring components index.
 *
 * @author Stephane Nicoll
 * @since 5.0
 */
public class SpringComponentsIndex {

	private final MultiValueMap<String, String> index;

	SpringComponentsIndex(MultiValueMap<String, String> index) {
		this.index = index;
	}

	/**
	 * Return the components that are associated with the specified target.
	 * @param target a target type (e.g an annotation type)
	 * @return the FQNs of components associated with the target
	 */
	public Set<String> getComponents(String target) {
		List<String> result = this.index.get(target);
		return (result != null ? new LinkedHashSet<>(result) : Collections.emptySet());
	}

}
