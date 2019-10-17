package com.hy.demo.app.transaction;

import com.hy.demo.rocketmq.annotation.MQTransactionProducer;
import com.hy.demo.rocketmq.base.AbstractMQTransactionProducer;
import org.apache.rocketmq.client.producer.LocalTransactionState;

/**
 * @program: demo
 * @description: 测试事务发送类
 * @author: Huang Ying
 * @create: 2019-10-11 16:02
 **/
@MQTransactionProducer(producerGroup = "demo",topic = "Test",tag = "Hello")
public class DemoListener extends AbstractMQTransactionProducer {
	@Override
	public LocalTransactionState getLocalTransaction(Object o) {
		System.out.println(o);
		return LocalTransactionState.COMMIT_MESSAGE;
	}

	@Override
	public LocalTransactionState checkLocalTransaction(Object o) {
		System.out.println(o);
		return LocalTransactionState.COMMIT_MESSAGE;
	}
}
