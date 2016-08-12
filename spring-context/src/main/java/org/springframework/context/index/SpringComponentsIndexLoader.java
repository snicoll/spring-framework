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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Components index loading mechanism for internal use within the framework.
 *
 * @author Stephane Nicoll
 * @since 5.0
 */
public class SpringComponentsIndexLoader {


	private static final Log logger = LogFactory.getLog(SpringComponentsIndexLoader.class);

	/**
	 * The location to look for components.
	 * <p>Can be present in multiple JAR files.
	 */
	public static final String COMPONENTS_RESOURCE_LOCATION = "META-INF/spring.components";


	/**
	 * Load and instantiate the {@link SpringComponentsIndex} from
	 * {@value #COMPONENTS_RESOURCE_LOCATION}, using the given class loader.
	 * @param classLoader the ClassLoader to use for loading (can be {@code null} to use the default)
	 * @see #loadIndexes
	 * @throws IllegalArgumentException if any module index cannot
	 * be loaded or if an error occurs while creating {@link SpringComponentsIndex}
	 */
	public static SpringComponentsIndex loadIndex(ClassLoader classLoader) {
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = SpringComponentsIndexLoader.class.getClassLoader();
		}
		List<Properties> indexes = loadIndexes(classLoaderToUse);
		if (logger.isTraceEnabled()) {
			logger.trace("Loaded " + indexes.size() + "] index(es)");
		}
		MultiValueMap<String, String> index = new LinkedMultiValueMap<>();
		for (Properties properties : indexes) {
			for (Map.Entry<Object, Object> entries : properties.entrySet()) {
				String type = (String) entries.getKey();
				String[] stereotypes = ((String) entries.getValue()).split(",");
				for (String stereotype : stereotypes) {
					index.add(stereotype, type);
				}
			}
		}
		return new SpringComponentsIndex(index);
	}

	/**
	 * Load the component indexes from {@value #COMPONENTS_RESOURCE_LOCATION}, using the
	 * given class loader.
	 * @param classLoader the ClassLoader to use for loading resources; can be
	 * {@code null} to use the default
	 * @see #loadIndex(ClassLoader)
	 * @throws IllegalArgumentException if an error occurs while loading indexes
	 */
	public static List<Properties> loadIndexes(ClassLoader classLoader) {
		try {
			Enumeration<URL> urls = (classLoader != null ? classLoader.getResources(COMPONENTS_RESOURCE_LOCATION) :
					ClassLoader.getSystemResources(COMPONENTS_RESOURCE_LOCATION));
			List<Properties> result = new ArrayList<>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(url));
				result.add(properties);
			}
			return result;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load indexes from location ["
					+ COMPONENTS_RESOURCE_LOCATION + "]", ex);
		}
	}

}
