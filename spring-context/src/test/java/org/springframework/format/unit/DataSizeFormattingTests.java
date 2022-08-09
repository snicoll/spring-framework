/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.format.unit;

import org.junit.jupiter.api.Test;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.annotation.DataSizeFormat;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.DataBinder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Formatting tests for {@link DataSizeFormatter}.
 *
 * @author Stephane Nicoll
 */
class DataSizeFormattingTests {

	private final FormattingConversionService conversionService;

	private final DataBinder binder;

	public DataSizeFormattingTests() {
		this.conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(this.conversionService);
		this.conversionService.addFormatter(new DataSizeFormatter());
		this.conversionService.addFormatterForFieldAnnotation(
				new DataSizeFormatAnnotationFormatterFactory());
		this.binder = new DataBinder(new TestBean());
		this.binder.setConversionService(this.conversionService);
	}

	@Test
	void bindWithDefaultSettingsAndNoUnitDefaultToBytes() {
		assertThat(bindToTestBean("dataSize", "10").dataSize)
				.isEqualTo(DataSize.ofBytes(10));
	}

	@Test
	void bindWithDefaultSettingsAndUnitUsesUnit() {
		assertThat(bindToTestBean("dataSize", "10MB").dataSize)
				.isEqualTo(DataSize.ofMegabytes(10));
	}

	@Test
	void bindWithCustomUnitAndNoUnitUsesCustomUnit() {
		assertThat(bindToTestBean("dataSizeKb", "10").dataSizeKb)
				.isEqualTo(DataSize.ofKilobytes(10));
	}

	@Test
	void bindWithCustomUnitAndUnitUsesUnit() {
		assertThat(bindToTestBean("dataSizeKb", "10MB").dataSizeKb)
				.isEqualTo(DataSize.ofMegabytes(10));
	}

	@Test
	void convertWhenBadFormatShouldThrowException() {
		assertThatExceptionOfType(ConversionFailedException.class).isThrownBy(() ->
						this.conversionService.convert("10WB", DataSize.class))
				.havingCause().isInstanceOf(IllegalArgumentException.class)
				.withMessageContaining("'10WB' is not a valid data size");
	}

	@Test
	void convertWhenEmptyShouldReturnNull() {
		assertThat(bindToTestBean("dataSize", "").dataSize).isNull();
	}

	@Test
	void formatWithRendersWithDefaultUnit() {
		bind("dataSizeKb", "10");
		assertThat(this.binder.getBindingResult().getFieldValue("dataSizeKb"))
				.isEqualTo("10KB");
	}

	@Test
	void formatWithRenderUnitDisabled() {
		bind("dataSizeNoUnit", "1024B");
		assertThat(this.binder.getBindingResult().getFieldValue("dataSizeNoUnit"))
				.isEqualTo("1024");
	}

	private TestBean bindToTestBean(String fieldName, String source) {
		bind(fieldName, source);
		return (TestBean) this.binder.getTarget();
	}

	private void bind(String fieldName, String source) {
		MutablePropertyValues propertyValues = new MutablePropertyValues();
		propertyValues.add(fieldName, source);
		this.binder.bind(propertyValues);
		assertThat(this.binder.getBindingResult().getErrorCount()).isEqualTo(0);
	}

	@SuppressWarnings("unused")
	static class TestBean {

		private DataSize dataSize;

		@DataSizeFormat(defaultUnit = DataUnit.KILOBYTES)
		private DataSize dataSizeKb;

		@DataSizeFormat(renderUnit = false)
		private DataSize dataSizeNoUnit;

		public DataSize getDataSize() {
			return this.dataSize;
		}

		public void setDataSize(DataSize dataSize) {
			this.dataSize = dataSize;
		}

		public DataSize getDataSizeKb() {
			return this.dataSizeKb;
		}

		public void setDataSizeKb(DataSize dataSizeKb) {
			this.dataSizeKb = dataSizeKb;
		}

		public DataSize getDataSizeNoUnit() {
			return this.dataSizeNoUnit;
		}

		public void setDataSizeNoUnit(DataSize dataSizeNoUnit) {
			this.dataSizeNoUnit = dataSizeNoUnit;
		}
	}

}
