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

package org.springframework.context.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.index.SpringComponentsIndex;
import org.springframework.context.index.SpringComponentsIndexLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Stephane Nicoll
 * @since 5.0
 */
public class SpringComponentsIndexCandidateComponentProvider implements ResourceLoaderAware {

	protected Log logger = LogFactory.getLog(SpringComponentsIndexCandidateComponentProvider.class);

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private MetadataReaderFactory metadataReaderFactory =
			new CachingMetadataReaderFactory(this.resourcePatternResolver);

	//TODO
	private SpringComponentsIndex index;

	public SpringComponentsIndexCandidateComponentProvider(SpringComponentsIndex index) {
		// TODO
		//Assert.notNull(index, "Index must not be null");
		this.index = index;
	}

	protected SpringComponentsIndex getIndex() {
		if (this.index == null) {
			this.index = SpringComponentsIndexLoader.loadIndex(getClass().getClassLoader());
		}
		return this.index;
	}

	/**
	 * Set the ResourceLoader to use for resource locations.
	 * This will typically be a ResourcePatternResolver implementation.
	 * <p>Default is PathMatchingResourcePatternResolver, also capable of
	 * resource pattern resolving through the ResourcePatternResolver interface.
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
	}

	/**
	 * Scan the index for candidate components.
	 * @return a corresponding Set of autodetected bean definitions
	 */
	public Set<BeanDefinition> findCandidateComponents() {
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		boolean debugEnabled = logger.isDebugEnabled();
		Set<String> components = getIndex().getComponents(Component.class.getName());
		for (String component : components) {
			try {
				//MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(component);
				GenericBeanDefinition sbd = new GenericBeanDefinition();
				sbd.setBeanClassName(component);
				// TODO: no ASM meta-data?
				// TODO: no isCandidateCompoent check?
				if (debugEnabled) {
					logger.debug("Identified candidate component class: " + component);
				}
				candidates.add(sbd);
			}
			catch (Throwable ex) {
				throw new BeanDefinitionStoreException(
						"Failed to read candidate component class: " + component, ex);
			}
		}
		return candidates;
	}

	/**
	 * Determine whether the given bean definition qualifies as candidate.
	 * <p>The default implementation checks whether the class is concrete
	 * (i.e. not abstract and not an interface). Can be overridden in subclasses.
	 * @param beanDefinition the bean definition to check
	 * @return whether the bean definition qualifies as a candidate component
	 */
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return (beanDefinition.getMetadata().isConcrete() && beanDefinition.getMetadata().isIndependent());
	}

}
