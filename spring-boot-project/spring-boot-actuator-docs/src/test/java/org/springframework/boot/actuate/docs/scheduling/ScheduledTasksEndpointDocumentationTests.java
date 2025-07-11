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

package org.springframework.boot.actuate.docs.scheduling;

import java.time.Instant;
import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.docs.MockMvcEndpointDocumentationTests;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.replacePattern;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

/**
 * Tests for generating documentation describing the {@link ScheduledTasksEndpoint}.
 *
 * @author Andy Wilkinson
 */
class ScheduledTasksEndpointDocumentationTests extends MockMvcEndpointDocumentationTests {

	@Test
	void scheduledTasks() {
		assertThat(this.mvc.get().uri("/actuator/scheduledtasks")).hasStatusOk()
			.apply(document("scheduled-tasks",
					preprocessResponse(replacePattern(
							Pattern.compile("org.*\\.ScheduledTasksEndpointDocumentationTests\\$TestConfiguration"),
							"com.example.Processor")),
					responseFields(fieldWithPath("cron").description("Cron tasks, if any."),
							targetFieldWithPrefix("cron.[]."),
							nextExecutionWithPrefix("cron.[].").description("Time of the next scheduled execution."),
							fieldWithPath("cron.[].expression").description("Cron expression."),
							fieldWithPath("fixedDelay").description("Fixed delay tasks, if any."),
							targetFieldWithPrefix("fixedDelay.[]."), initialDelayWithPrefix("fixedDelay.[]."),
							nextExecutionWithPrefix("fixedDelay.[]."),
							fieldWithPath("fixedDelay.[].interval")
								.description("Interval, in milliseconds, between the end of the last"
										+ " execution and the start of the next."),
							fieldWithPath("fixedRate").description("Fixed rate tasks, if any."),
							targetFieldWithPrefix("fixedRate.[]."),
							fieldWithPath("fixedRate.[].interval")
								.description("Interval, in milliseconds, between the start of each execution."),
							initialDelayWithPrefix("fixedRate.[]."), nextExecutionWithPrefix("fixedRate.[]."),
							fieldWithPath("custom").description("Tasks with custom triggers, if any."),
							targetFieldWithPrefix("custom.[]."),
							fieldWithPath("custom.[].trigger").description("Trigger for the task."))
						.andWithPrefix("*.[].",
								fieldWithPath("lastExecution").description("Last execution of this task, if any.")
									.optional()
									.type(JsonFieldType.OBJECT))
						.andWithPrefix("*.[].lastExecution.", lastExecution())));
	}

	private FieldDescriptor targetFieldWithPrefix(String prefix) {
		return fieldWithPath(prefix + "runnable.target").description("Target that will be executed.");
	}

	private FieldDescriptor initialDelayWithPrefix(String prefix) {
		return fieldWithPath(prefix + "initialDelay").description("Delay, in milliseconds, before first execution.");
	}

	private FieldDescriptor nextExecutionWithPrefix(String prefix) {
		return fieldWithPath(prefix + "nextExecution.time")
			.description("Time of the next scheduled execution, if known.")
			.type(JsonFieldType.STRING)
			.optional();
	}

	private FieldDescriptor[] lastExecution() {
		return new FieldDescriptor[] {
				fieldWithPath("status").description("Status of the last execution (STARTED, SUCCESS, ERROR).")
					.type(JsonFieldType.STRING),
				fieldWithPath("time").description("Time of the last execution.").type(JsonFieldType.STRING),
				fieldWithPath("exception.type").description("Exception type thrown by the task, if any.")
					.type(JsonFieldType.STRING)
					.optional(),
				fieldWithPath("exception.message").description("Message of the exception thrown by the task, if any.")
					.type(JsonFieldType.STRING)
					.optional() };
	}

	@Configuration(proxyBeanMethods = false)
	@EnableScheduling
	static class TestConfiguration {

		@Bean
		ScheduledTasksEndpoint endpoint(Collection<ScheduledTaskHolder> holders) {
			return new ScheduledTasksEndpoint(holders);
		}

		@Scheduled(cron = "0 0 0/3 1/1 * ?")
		void processOrders() {

		}

		@Scheduled(fixedDelay = 5000, initialDelay = 0)
		void purge() {

		}

		@Scheduled(fixedRate = 3000, initialDelay = 10000)
		void retrieveIssues() {

		}

		@Bean
		SchedulingConfigurer schedulingConfigurer() {
			return (registrar) -> {
				registrar.setTaskScheduler(new TestTaskScheduler());
				registrar.addTriggerTask(new CustomTriggeredRunnable(), new CustomTrigger());
			};
		}

		static class CustomTrigger implements Trigger {

			@Override
			public Instant nextExecution(TriggerContext triggerContext) {
				return Instant.now();
			}

		}

		static class CustomTriggeredRunnable implements Runnable {

			@Override
			public void run() {
				throw new IllegalStateException("Failed while running custom task");
			}

		}

		static class TestTaskScheduler extends SimpleAsyncTaskScheduler {

			TestTaskScheduler() {
				setThreadNamePrefix("test-");
				// do not log task errors
				setErrorHandler((throwable) -> {
				});
			}

		}

	}

}
