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

package org.springframework.boot.actuate.endpoint.invoke.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.boot.actuate.endpoint.invoke.OperationParameters;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link OperationMethod}.
 *
 * @author Phillip Webb
 */
class OperationMethodTests {

	private static final Predicate<Parameter> NON_OPTIONAL = (parameter) -> false;

	private final Method exampleMethod = ReflectionUtils.findMethod(getClass(), "example", String.class);

	@Test
	void createWhenMethodIsNullShouldThrowException() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new OperationMethod(null, OperationType.READ, NON_OPTIONAL))
			.withMessageContaining("'method' must not be null");
	}

	@Test
	void createWhenOperationTypeIsNullShouldThrowException() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new OperationMethod(this.exampleMethod, null, NON_OPTIONAL))
			.withMessageContaining("'operationType' must not be null");
	}

	@Test
	void getMethodShouldReturnMethod() {
		OperationMethod operationMethod = new OperationMethod(this.exampleMethod, OperationType.READ, NON_OPTIONAL);
		assertThat(operationMethod.getMethod()).isEqualTo(this.exampleMethod);
	}

	@Test
	void getOperationTypeShouldReturnOperationType() {
		OperationMethod operationMethod = new OperationMethod(this.exampleMethod, OperationType.READ, NON_OPTIONAL);
		assertThat(operationMethod.getOperationType()).isEqualTo(OperationType.READ);
	}

	@Test
	void getParametersShouldReturnParameters() {
		OperationMethod operationMethod = new OperationMethod(this.exampleMethod, OperationType.READ, NON_OPTIONAL);
		OperationParameters parameters = operationMethod.getParameters();
		assertThat(parameters.getParameterCount()).isOne();
		assertThat(parameters.iterator().next().getName()).isEqualTo("name");
	}

	String example(String name) {
		return name;
	}

}
