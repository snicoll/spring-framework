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

package org.springframework.test.context.bean.override;

import java.lang.reflect.Field;
import java.util.Objects;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.style.ToStringCreator;
import org.springframework.lang.Nullable;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.ObjectUtils;

/**
 * Metadata for Bean Override injection points.
 *
 * <p><strong>WARNING</strong>: implementations that add fields must take care
 * to implement correct {@link Object#equals(Object) equals} and
 * {@link Object#hashCode() hashCode} methods since override metadata is stored
 * in a {@link org.springframework.test.context.ContextCustomizer} and thus
 * form part of the {@link MergedContextConfiguration} which is used as a cache
 * key.
 *
 * @author Simon Baslé
 * @since 6.2
 */
public abstract class OverrideMetadata {

	protected static final int HASHCODE_MULTIPLIER = 31;

	private final Field field;

	private final ResolvableType fieldType;

	private final BeanOverrideStrategy strategy;


	protected OverrideMetadata(Field field, ResolvableType fieldType,
			BeanOverrideStrategy strategy) {

		this.field = field;
		this.fieldType = fieldType;
		this.strategy = strategy;
	}


	/**
	 * The human-readable type of Bean Override this metadata represents.
	 * <p>This should be one or two words typically displayed in error messages.
	 * A good option is a string representation of the associated annotation
	 * &mdash; for example, {@code @TestBean}.
	 */
	public abstract String getType();

	/**
	 * Return the expected bean name to override.
	 * <p>Typically, this is either explicitly set in a concrete annotation or
	 * inferred from the annotated field's name.
	 * @return the expected bean name
	 */
	protected String getExpectedBeanName() {
		return this.field.getName();
	}

	/**
	 * Return the annotated {@link Field}.
	 */
	public final Field getField() {
		return this.field;
	}

	/**
	 * Return the bean {@link ResolvableType type} to override.
	 */
	public final ResolvableType getFieldType() {
		return this.fieldType;
	}

	/**
	 * Return the {@link BeanOverrideStrategy} for this instance, as a hint on
	 * how and when the override instance should be created.
	 */
	public final BeanOverrideStrategy getStrategy() {
		return this.strategy;
	}

	/**
	 * Create an override instance from this {@link OverrideMetadata},
	 * optionally provided with an existing {@link BeanDefinition} and/or an
	 * original instance, that is a singleton or an early wrapped instance.
	 * @param beanName the name of the bean being overridden
	 * @param existingBeanDefinition an existing bean definition for that bean
	 * name, or {@code null} if not relevant
	 * @param existingBeanInstance an existing instance for that bean name,
	 * for wrapping purposes, or {@code null} if irrelevant
	 * @return the instance with which to override the bean
	 */
	protected abstract Object createOverride(String beanName, @Nullable BeanDefinition existingBeanDefinition,
			@Nullable Object existingBeanInstance);

	/**
	 * Optionally track objects created by this {@link OverrideMetadata}.
	 * <p>The default is not to track, but this can be overridden in subclasses.
	 * @param override the bean override instance to track
	 * @param trackingBeanRegistry the registry in which trackers can
	 * optionally be registered
	 */
	protected void track(Object override, SingletonBeanRegistry trackingBeanRegistry) {
		// NO-OP
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
			return false;
		}
		OverrideMetadata that = (OverrideMetadata) obj;
		return Objects.equals(this.strategy, that.strategy) &&
				Objects.equals(this.field, that.field) &&
				Objects.equals(this.fieldType, that.fieldType);
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = HASHCODE_MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.strategy);
		result = HASHCODE_MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.field);
		result = HASHCODE_MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.fieldType);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("type", getType())
				.append("strategy", this.strategy)
				.append("field", this.field)
				.append("fieldType", this.fieldType)
				.toString();
	}

}
