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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.persistence.Converter;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import org.springframework.context.index.metadata.PropertiesMarshaller;
import org.springframework.context.index.metadata.SpringComponentsMetadata;
import org.springframework.context.index.sample.SampleComponent;
import org.springframework.context.index.sample.SampleController;
import org.springframework.context.index.sample.SampleMetaController;
import org.springframework.context.index.sample.SampleNone;
import org.springframework.context.index.sample.SampleService;
import org.springframework.context.index.sample.jpa.SampleConverter;
import org.springframework.context.index.sample.jpa.SampleEmbeddable;
import org.springframework.context.index.sample.jpa.SampleEntity;
import org.springframework.context.index.sample.jpa.SampleMappedSuperClass;
import org.springframework.context.index.test.Metadata;
import org.springframework.context.index.test.TestCompiler;
import org.springframework.stereotype.Component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link SpringComponentsIndexer}.
 *
 * @author Stephane Nicoll
 */
public class SpringComponentsIndexerTests {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private TestCompiler compiler;

	@Before
	public void createCompiler() throws IOException {
		this.compiler = new TestCompiler(this.temporaryFolder);
	}

	@Test
	public void noCandidate() throws IOException {
		SpringComponentsMetadata metadata = compile(SampleNone.class);
		assertThat(metadata.getItems(), hasSize(0));
	}

	@Test
	public void noAnnotation() throws IOException {
		SpringComponentsMetadata metadata = compile(SpringComponentsIndexerTests.class);
		assertThat(metadata.getItems(), hasSize(0));
	}

	@Test
	public void stereotypeComponent() throws IOException {
		testComponent(SampleComponent.class);
	}

	@Test
	public void stereotypeService() throws IOException {
		testComponent(SampleService.class);
	}

	@Test
	public void stereotypeController() throws IOException {
		testComponent(SampleController.class);
	}

	@Test
	public void stereotypeControllerMetaAnnotation() throws IOException {
		testComponent(SampleMetaController.class);
	}

	@Test
	public void sampleEntity() throws IOException {
		testSingleComponent(SampleEntity.class, Entity.class);
	}

	@Test
	public void sampleMappedSuperClass() throws IOException {
		testSingleComponent(SampleMappedSuperClass.class, MappedSuperclass.class);
	}

	@Test
	public void sampleEmbeddable() throws IOException {
		testSingleComponent(SampleEmbeddable.class, Embeddable.class);
	}

	@Test
	public void sampleConverter() throws IOException {
		testSingleComponent(SampleConverter.class, Converter.class);
	}

	private void testComponent(Class<?>... classes) throws IOException {
		SpringComponentsMetadata metadata = compile(classes);
		for (Class<?> c : classes) {
			assertThat(metadata, Metadata.hasComponent(c, Component.class));
		}
		assertThat(metadata.getItems(), hasSize(classes.length));
	}

	private void testSingleComponent(Class<?> target, Class<?>... stereotypes) throws IOException {
		SpringComponentsMetadata metadata = compile(target);
		assertThat(metadata, Metadata.hasComponent(target, stereotypes));
		assertThat(metadata.getItems(), hasSize(stereotypes.length));
	}

	private SpringComponentsMetadata compile(Class<?>... types) throws IOException {
		SpringComponentsIndexer processor = new SpringComponentsIndexer();
		this.compiler.getTask(types).call(processor);
		return readGeneratedMetadata(this.compiler.getOutputLocation());
	}

	private SpringComponentsMetadata readGeneratedMetadata(File outputLocation) {
		try {
			File metadataFile = new File(outputLocation,
					MetadataStore.METADATA_PATH);
			if (metadataFile.isFile()) {
				return new PropertiesMarshaller()
						.read(new FileInputStream(metadataFile));
			}
			else {
				return new SpringComponentsMetadata();
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to read metadata from disk", e);
		}
	}

}
