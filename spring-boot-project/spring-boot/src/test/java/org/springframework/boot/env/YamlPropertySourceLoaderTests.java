/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.env;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.boot.testsupport.classpath.resources.WithResource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link YamlPropertySourceLoader}.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
class YamlPropertySourceLoaderTests {

	private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

	@Test
	void load() throws Exception {
		ByteArrayResource resource = new ByteArrayResource("foo:\n  bar: spam".getBytes());
		PropertySource<?> source = this.loader.load("resource", resource).get(0);
		assertThat(source).isNotNull();
		assertThat(source.getProperty("foo.bar")).isEqualTo("spam");
	}

	@Test
	void orderedItems() throws Exception {
		StringBuilder yaml = new StringBuilder();
		List<String> expected = new ArrayList<>();
		for (char c = 'a'; c <= 'z'; c++) {
			yaml.append(c).append(": value").append(c).append("\n");
			expected.add(String.valueOf(c));
		}
		ByteArrayResource resource = new ByteArrayResource(yaml.toString().getBytes());
		EnumerablePropertySource<?> source = (EnumerablePropertySource<?>) this.loader.load("resource", resource)
			.get(0);
		assertThat(source).isNotNull();
		assertThat(source.getPropertyNames()).isEqualTo(StringUtils.toStringArray(expected));
	}

	@Test
	void mergeItems() throws Exception {
		StringBuilder yaml = new StringBuilder();
		yaml.append("foo:\n  bar: spam\n");
		yaml.append("---\n");
		yaml.append("foo:\n  baz: wham\n");
		ByteArrayResource resource = new ByteArrayResource(yaml.toString().getBytes());
		List<PropertySource<?>> loaded = this.loader.load("resource", resource);
		assertThat(loaded).hasSize(2);
		assertThat(loaded.get(0).getProperty("foo.bar")).isEqualTo("spam");
		assertThat(loaded.get(1).getProperty("foo.baz")).isEqualTo("wham");
	}

	@Test
	void timestampLikeItemsDoNotBecomeDates() throws Exception {
		ByteArrayResource resource = new ByteArrayResource("foo: 2015-01-28".getBytes());
		PropertySource<?> source = this.loader.load("resource", resource).get(0);
		assertThat(source).isNotNull();
		assertThat(source.getProperty("foo")).isEqualTo("2015-01-28");
	}

	@Test
	@WithResource(name = "test-yaml.yml", content = """
			a: b
			---
			c: d
			e: f
			""")
	void loadOriginAware() throws Exception {
		Resource resource = new ClassPathResource("test-yaml.yml");
		List<PropertySource<?>> loaded = this.loader.load("resource", resource);
		for (PropertySource<?> source : loaded) {
			EnumerablePropertySource<?> enumerableSource = (EnumerablePropertySource<?>) source;
			for (String name : enumerableSource.getPropertyNames()) {
				System.out.println(name + " = " + enumerableSource.getProperty(name));
			}
		}
	}

}
