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

package org.springframework.format.unit;

import java.text.ParseException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DataSizeFormatter}.
 *
 * @author Stephane Nicoll
 */
public class DataSizeFormatterTests {

	private final DataSizeFormatter formatter = new DataSizeFormatter();

	@Test
	void parseWhenSimpleBytesShouldReturnDataSize() {
		assertThat(parse("10B")).isEqualTo(DataSize.ofBytes(10));
		assertThat(parse("+10B")).isEqualTo(DataSize.ofBytes(10));
		assertThat(parse("-10B")).isEqualTo(DataSize.ofBytes(-10));
	}

	@Test
	void parseWhenSimpleKilobytesShouldReturnDataSize() {
		assertThat(parse("10KB")).isEqualTo(DataSize.ofKilobytes(10));
		assertThat(parse("+10KB")).isEqualTo(DataSize.ofKilobytes(10));
		assertThat(parse("-10KB")).isEqualTo(DataSize.ofKilobytes(-10));
	}

	@Test
	void parseWhenSimpleMegabytesShouldReturnDataSize() {
		assertThat(parse("10MB")).isEqualTo(DataSize.ofMegabytes(10));
		assertThat(parse("+10MB")).isEqualTo(DataSize.ofMegabytes(10));
		assertThat(parse("-10MB")).isEqualTo(DataSize.ofMegabytes(-10));
	}

	@Test
	void parseWhenSimpleGigabytesShouldReturnDataSize() {
		assertThat(parse("10GB")).isEqualTo(DataSize.ofGigabytes(10));
		assertThat(parse("+10GB")).isEqualTo(DataSize.ofGigabytes(10));
		assertThat(parse("-10GB")).isEqualTo(DataSize.ofGigabytes(-10));
	}

	@Test
	void parseWhenSimpleTerabytesShouldReturnDataSize() {
		assertThat(parse("10TB")).isEqualTo(DataSize.ofTerabytes(10));
		assertThat(parse("+10TB")).isEqualTo(DataSize.ofTerabytes(10));
		assertThat(parse("-10TB")).isEqualTo(DataSize.ofTerabytes(-10));
	}

	@Test
	void parseWhenSimpleWithoutSuffixShouldReturnDataSize() {
		assertThat(parse("10")).isEqualTo(DataSize.ofBytes(10));
		assertThat(parse("+10")).isEqualTo(DataSize.ofBytes(10));
		assertThat(parse("-10")).isEqualTo(DataSize.ofBytes(-10));
	}

	@Test
	void parseWhenSimpleWithoutSuffixButWithCustomDefaultUnitShouldReturnDataSize() {
		DataSizeFormatter customFormatter = new DataSizeFormatter(DataUnit.KILOBYTES, false);
		assertThat(parse(customFormatter, "10")).isEqualTo(DataSize.ofKilobytes(10));
		assertThat(parse(customFormatter, "+10")).isEqualTo(DataSize.ofKilobytes(10));
		assertThat(parse(customFormatter, "-10")).isEqualTo(DataSize.ofKilobytes(-10));
	}

	private DataSize parse(String source) {
		return parse(this.formatter, source);
	}

	private DataSize parse(DataSizeFormatter formatter, String source) {
		try {
			return formatter.parse(source, Locale.ENGLISH);
		}
		catch (ParseException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
