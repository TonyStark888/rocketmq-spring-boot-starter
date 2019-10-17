package com.hy.demo.app.consumer;

import com.hy.demo.rocketmq.annotation.MQConsumer;
import com.hy.demo.rocketmq.base.AbstractMQPushConsumer;

import java.util.Map;


/**
 *
 * @author huang
 * @date 2019-09-02
 */
@MQConsumer(consumerGroup = "consumerTest", topic = "Test", tag = "Hello")
public class DemoConsumer extends AbstractMQPushConsumer<String> {
	@Override
	public boolean process(String message, Map<String, Object> extMap) {
		System.out.println(message);
		return true;
	}
}
