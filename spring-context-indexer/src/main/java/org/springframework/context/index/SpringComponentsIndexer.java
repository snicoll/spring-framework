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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.springframework.context.index.metadata.ItemMetadata;
import org.springframework.context.index.metadata.SpringComponentsMetadata;

/**
 * Annotation {@link Processor} that writes {@link SpringComponentsMetadata}
 * file for spring components.
 *
 * @author Stephane Nicoll
 * @since 5.0
 */
@SupportedAnnotationTypes({"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SpringComponentsIndexer extends AbstractProcessor {

	private static final List<TargetAnnotation> TARGET_ANNOTATIONS = Arrays.asList(
			new TargetAnnotation("org.springframework.stereotype.Component", true),
			new TargetAnnotation("javax.persistence.Entity", false),
			new TargetAnnotation("javax.persistence.Embeddable", false),
			new TargetAnnotation("javax.persistence.MappedSuperclass", false),
			new TargetAnnotation("javax.persistence.Converter", false)
	);

	private MetadataStore metadataStore;

	private MetadataCollector metadataCollector;

	private TypeUtils typeUtils;

	private Elements elements;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		this.typeUtils = new TypeUtils(env);
		this.elements = env.getElementUtils();
		this.metadataStore = new MetadataStore(env);
		this.metadataCollector = new MetadataCollector(env,
				this.metadataStore.readMetadata());
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		this.metadataCollector.processing(roundEnv);
		for (Element element : roundEnv.getRootElements()) {
			processElement(element);
		}

		if (roundEnv.processingOver()) {
			writeMetaData();
		}
		return false;
	}

	private void processElement(Element element) {
		Set<String> annotations = collectAnnotations(element);
		if (!annotations.isEmpty()) {
			this.metadataCollector.add(new ItemMetadata(
					this.typeUtils.getType(element), annotations));
		}
	}

	private Set<String> collectAnnotations(Element element) {
		return TARGET_ANNOTATIONS.stream()
				.map(e -> resolveAnnotation(element, e.type, e.meta))
				.filter(Objects::nonNull)
				.map(Object::toString)
				.collect(Collectors.toSet());
	}


	protected SpringComponentsMetadata writeMetaData() {
		SpringComponentsMetadata metadata = this.metadataCollector.getMetadata();
		if (!metadata.getItems().isEmpty()) {
			try {
				this.metadataStore.writeMetadata(metadata);
			}
			catch (IOException ex) {
				throw new IllegalStateException("Failed to write metadata", ex);
			}
			return metadata;
		}
		return null;
	}

	public String resolveAnnotation(Element element, String type, boolean meta) {
		return getAnnotation(element, type, meta) != null ? type : null;
	}

	private AnnotationMirror getAnnotation(Element element, String type, boolean meta) {
		if (element != null) {
			Set<Element> seen = new HashSet<>();
			for (AnnotationMirror annotation : this.elements.getAllAnnotationMirrors(element)) {
				AnnotationMirror result = getAnnotation(seen, annotation, type, meta);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private AnnotationMirror getAnnotation(Set<Element> seen, AnnotationMirror annotation, String type, boolean meta) {
		if (type.equals(annotation.getAnnotationType().toString())) {
			return annotation;
		}
		Element element = filterElement(seen, annotation);
		if (meta && element != null) {
			List<? extends AnnotationMirror> nestedAnnotations =
					this.elements.getAllAnnotationMirrors(element);
			for (AnnotationMirror nestedAnnotation : nestedAnnotations) {
				AnnotationMirror result = getAnnotation(seen, nestedAnnotation, type, meta);
				if (result != null) {
					return result;
				}

			}
		}
		return null;
	}

	private Element filterElement(Set<Element> seen, AnnotationMirror nestedAnnotation) {
		Element element = nestedAnnotation.getAnnotationType().asElement();
		if (seen.contains(element)) {
			return null;
		}
		seen.add(element);
		return (!element.toString().startsWith("java.lang") ? element : null);
	}

	private static class TargetAnnotation {

		private final String type;

		private final boolean meta;

		public TargetAnnotation(String type, boolean meta) {
			this.type = type;
			this.meta = meta;
		}
	}

}
