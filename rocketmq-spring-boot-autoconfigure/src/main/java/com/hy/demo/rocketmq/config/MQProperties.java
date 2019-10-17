package com.hy.demo.rocketmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.rocketmq")
public class MQProperties {
	/**
	 * RocketMQ服务端地址
	 */
	private String namesrv;

	/**
	 * 生产者组
	 */
	private String producerGroup;

	/**
	 * 消费者组
	 */
	private String consumerGroup;

}
