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

package org.springframework.boot.actuate.docs.sbom;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.docs.MockMvcEndpointDocumentationTests;
import org.springframework.boot.actuate.sbom.SbomEndpoint;
import org.springframework.boot.actuate.sbom.SbomEndpointWebExtension;
import org.springframework.boot.actuate.sbom.SbomProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

/**
 * Tests for generating documentation describing the {@link SbomEndpoint}.
 *
 * @author Moritz Halbritter
 */
class SbomEndpointDocumentationTests extends MockMvcEndpointDocumentationTests {

	@Test
	void sbom() {
		assertThat(this.mvc.get().uri("/actuator/sbom")).hasStatusOk()
			.apply(MockMvcRestDocumentation.document("sbom",
					responseFields(fieldWithPath("ids").description("An array of available SBOM ids."))));
	}

	@Test
	void sboms() {
		assertThat(this.mvc.get().uri("/actuator/sbom/application")).hasStatusOk()
			.apply(MockMvcRestDocumentation.document("sbom/id"));
	}

	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

		@Bean
		SbomProperties sbomProperties() {
			SbomProperties properties = new SbomProperties();
			properties.getApplication()
				.setLocation("classpath:org/springframework/boot/actuate/docs/sbom/cyclonedx.json");
			return properties;
		}

		@Bean
		SbomEndpoint endpoint(SbomProperties properties, ResourceLoader resourceLoader) {
			return new SbomEndpoint(properties, resourceLoader);
		}

		@Bean
		SbomEndpointWebExtension sbomEndpointWebExtension(SbomEndpoint endpoint, SbomProperties properties) {
			return new SbomEndpointWebExtension(endpoint, properties);
		}

	}

}
