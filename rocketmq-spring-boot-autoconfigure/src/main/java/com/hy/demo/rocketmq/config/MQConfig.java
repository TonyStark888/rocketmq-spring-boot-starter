package com.hy.demo.rocketmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author huang
 */
@Configuration
public class MQConfig {
	@Bean
	public MQProperties mqProperties() {
		return new MQProperties();
	}

	@Bean
	public MQProducer mqProducer() {
		return new MQProducer();
	}

	@Bean
	public RocketMQAnnotationScan mqAnnotation() {
		return new RocketMQAnnotationScan();
	}
}
