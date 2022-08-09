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

import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.format.annotation.DataSizeFormat;
import org.springframework.util.unit.DataSize;

/**
 * Formats fields annotated with the {@link DataSizeFormat} annotation.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public class DataSizeFormatAnnotationFormatterFactory implements AnnotationFormatterFactory<DataSizeFormat> {

	@Override
	public Set<Class<?>> getFieldTypes() {
		return Set.of(DataSize.class);
	}

	@Override
	public Printer<?> getPrinter(DataSizeFormat annotation, Class<?> fieldType) {
		return new DataSizeFormatter(annotation.defaultUnit(), annotation.renderUnit());
	}

	@Override
	public Parser<?> getParser(DataSizeFormat annotation, Class<?> fieldType) {
		return new DataSizeFormatter(annotation.defaultUnit(), annotation.renderUnit());
	}
}
