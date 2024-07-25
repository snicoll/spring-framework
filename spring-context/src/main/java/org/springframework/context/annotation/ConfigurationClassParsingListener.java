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

package org.springframework.context.annotation;

/**
 * Interface that tools and external processes can implement to be notified
 * about the progress of configuration class parsing.
 *
 * @author Stephane Nicoll
 * @since 6.2
 * @see ConfigurationClassPostProcessor
 */
public interface ConfigurationClassParsingListener {

	/**
	 * A NoOp default implementation.
	 */
	ConfigurationClassParsingListener NO_OP = new ConfigurationClassParsingListener() {};


	/**
	 * Invoked when a component scan as completed, with the number of beans
	 * that are considered as <em>additions</em> to the bean factory. If a
	 * candidate found by component scan already exists in the bean factory,
	 * it is not considered.
	 * @param basePackageNames the base packages that were considered
	 * @param detectedBeans number of beans to add to the bean factory
	 */
	default void onComponentScan(String[] basePackageNames, int detectedBeans) {}

}
