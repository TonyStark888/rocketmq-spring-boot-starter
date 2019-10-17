package com.hy.demo.rocketmq.config;

import com.hy.demo.rocketmq.base.DelayTimeLevel;
import com.hy.demo.rocketmq.base.MessageBuilder;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * MQ生产者对象
 *
 * @author huang
 */
public class MQProducer {
	private static Logger logger = LoggerFactory.getLogger(MQProducer.class);

	private DefaultMQProducer producer;

	@Autowired
	private MQProperties mqProperties;

	@Autowired
	RocketMQAnnotationScan annotationScan;

	@EventListener(ContextStartedEvent.class)
	public void onContextStart() {
		// 普通消息的生产者实例
		this.producer = new DefaultMQProducer(mqProperties.getProducerGroup());
		producer.setNamesrvAddr(mqProperties.getNamesrv());
		try {
			producer.start();
			logger.info("producer: {} started", producer.getProducerGroup());
		} catch (MQClientException e) {
			logger.error("rocketmq producer start fail", e);
		}
	}

	//-------------------sync-------------------//

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public SendResult sendSync(String topic, String tag, Object message, String requestId) throws Exception {
		return producer.send(MessageBuilder.of(topic, tag, message, requestId).build());
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public SendResult sendSync(String topic, String tag, Object message, String requestId, DelayTimeLevel delay) throws Exception {
		return producer.send(MessageBuilder.of(topic, tag, message, requestId).delayTimeLevel(delay).build());
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public SendResult sendSyncOrderByKey(String topic, String tag, Object message, String requestId, Object orderBy) throws Exception {
		Objects.requireNonNull(orderBy);
		return producer.send(MessageBuilder.of(topic, tag, message, requestId).build(), new SelectMessageQueueByHash(), orderBy);
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public SendResult sendSyncOrderByKey(String topic, String tag, Object message, String requestId, Object orderBy, DelayTimeLevel delay) throws Exception {
		Objects.requireNonNull(orderBy);
		return producer.send(MessageBuilder.of(topic, tag, message, requestId).delayTimeLevel(delay).build(), new SelectMessageQueueByHash(), orderBy);
	}

	//-------------------async-------------------//

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public void sendAsync(String topic, String tag, Object message, String requestId, SendCallback callback) throws Exception {
		Objects.requireNonNull(requestId);
		producer.send(MessageBuilder.of(topic, tag, message, requestId).build(), callback);
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public void sendAsync(String topic, String tag, Object message, String requestId, SendCallback callback, DelayTimeLevel delay) throws Exception {
		producer.send(MessageBuilder.of(topic, tag, message, requestId).delayTimeLevel(delay).build(), callback);
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public void sendAsyncOrderByKey(String topic, String tag, Object message, String requestId, Object orderBy, SendCallback callback) throws Exception {
		Objects.requireNonNull(orderBy);
		producer.send(MessageBuilder.of(topic, tag, message, requestId).build(), new SelectMessageQueueByHash(), orderBy, callback);
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public void sendAsyncOrderByKey(String topic, String tag, Object message, String requestId, Object orderBy, SendCallback callback, DelayTimeLevel delay) throws Exception {
		Objects.requireNonNull(orderBy);
		producer.send(MessageBuilder.of(topic, tag, message, requestId).delayTimeLevel(delay).build(), new SelectMessageQueueByHash(), orderBy, callback);
	}

	//-------------------one-way-------------------//

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public void sendOneway(String topic, String tag, Object message, String requestId) throws Exception {
		producer.sendOneway(MessageBuilder.of(topic, tag, message, requestId).build());
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public void sendOneway(String topic, String tag, Object message, String requestId, DelayTimeLevel delay) throws Exception {
		producer.sendOneway(MessageBuilder.of(topic, tag, message, requestId).delayTimeLevel(delay).build());
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public void sendOnewayOrderByKey(String topic, String tag, Object message, String requestId, Object orderBy) throws Exception {
		Objects.requireNonNull(orderBy);
		producer.sendOneway(MessageBuilder.of(topic, tag, message, requestId).build(), new SelectMessageQueueByHash(), orderBy);
	}

	/**
	 * handler MQClientException, RemotingException, MQBrokerException
	 */
	public void sendOnewayOrderByKey(String topic, String tag, Object message, String requestId, Object orderBy, DelayTimeLevel delay) throws Exception {
		Objects.requireNonNull(orderBy);
		producer.sendOneway(MessageBuilder.of(topic, tag, message, requestId).delayTimeLevel(delay).build(), new SelectMessageQueueByHash(), orderBy);
	}

	//-------------------transaction-------------------//

	/**
	 * 事务消息发送
	 * 不支持延迟发送和批量发送
	 */
	public void sendMessageInTransaction(String topic, String tag, Object message, String requestId) throws Exception {
		TransactionMQProducer producer = annotationScan.getProducer(topic + "_" + tag);
		producer.sendMessageInTransaction(MessageBuilder.of(topic, tag, message, requestId).build(), message);
	}
}
