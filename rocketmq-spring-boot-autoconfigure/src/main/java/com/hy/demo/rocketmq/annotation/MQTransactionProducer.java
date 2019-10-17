package com.hy.demo.rocketmq.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RocketMQ事务消息注解
 * @author huang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MQTransactionProducer {

	/**
	 * 生产者发送组
	 * @return
	 */
	String producerGroup();

	/**
	 * 消息主题
	 * @return
	 */
	String topic();

	/**
	 * 消息标签
	 * @return
	 */
	String tag();
}
