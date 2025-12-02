/*
 * Copyright 2025 the original author or authors.
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

package com.zhouyu;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.observation.ToolCallingObservationContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
@Configuration
@Log4j2
public class ObservationConfiguration {
	@Bean
	public ObservationHandler<ToolCallingObservationContext> toolCallingObservationContextObservationHandler() {
		return new ObservationHandler<>() {
			@Override
			public boolean supportsContext(Observation.Context context) {
				return context instanceof ToolCallingObservationContext;
			}

			@Override
			public void onStart(ToolCallingObservationContext context) {
				ToolDefinition toolDefinition = context.getToolDefinition();
				log.info("开始执行工具: {} - {}", toolDefinition.name(), context.getToolCallArguments());
			}

			@Override
			public void onStop(ToolCallingObservationContext context) {
				ToolDefinition toolDefinition = context.getToolDefinition();
				log.info("工具执行结束: {} - {}", toolDefinition.name(), context.getToolCallResult());
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(name = "observationRegistry")
	public ObservationRegistry observationRegistry(ObjectProvider<ObservationHandler<?>> observationHandlerObjectProvider) {
		ObservationRegistry observationRegistry = ObservationRegistry.create();
		ObservationRegistry.ObservationConfig observationConfig = observationRegistry.observationConfig();
		observationHandlerObjectProvider.orderedStream().forEach(observationConfig::observationHandler);
		return observationRegistry;
	}

}
