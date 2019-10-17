package com.hy.demo.rocketmq.config;

import com.hy.demo.rocketmq.annotation.MQTransactionProducer;
import com.hy.demo.rocketmq.base.AbstractMQPushConsumer;
import com.hy.demo.rocketmq.base.AbstractMQTransactionProducer;
import com.hy.demo.rocketmq.base.MessageExtConst;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: rocketmq
 * @description: RocketMQ自定义注解扫描
 * @author: Huang Ying
 * @create: 2019-10-15
 **/
public class RocketMQAnnotationScan implements ApplicationContextAware {
	private static Logger log = LoggerFactory.getLogger(RocketMQAnnotationScan.class);

	private ConfigurableApplicationContext applicationContext;

	private Map<String, TransactionMQProducer> producerMap = new HashMap<>();

	@Autowired
	private MQProperties mqProperties;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	public TransactionMQProducer getProducer(String key) {
		TransactionMQProducer producer = producerMap.get(key);
		if (null == producer) {
			throw new NullPointerException("this transaction producer " + key + " has not init....");
		}
		return producer;
	}

	@EventListener(ContextStartedEvent.class)
	public void onContextStart() throws Exception {
		Map<String, Object> consumerBeans = applicationContext.getBeansWithAnnotation(com.hy.demo.rocketmq.annotation.MQConsumer.class);
		for (Map.Entry<String, Object> entry : consumerBeans.entrySet()) {
			publishConsumer(entry.getKey(), entry.getValue());
		}

		Map<String, Object> producerBeans = applicationContext.getBeansWithAnnotation(MQTransactionProducer.class);
		for (Map.Entry<String, Object> entry : producerBeans.entrySet()) {
			initProducer(entry.getKey(), entry.getValue());
		}
	}

	private void publishConsumer(String beanName, Object bean) throws Exception {
		com.hy.demo.rocketmq.annotation.MQConsumer mqConsumer = applicationContext.findAnnotationOnBean(beanName, com.hy.demo.rocketmq.annotation.MQConsumer.class);
		if (StringUtils.isEmpty(mqProperties.getNamesrv())) {
			throw new RuntimeException("name server address must be defined");
		}
		Assert.notNull(mqConsumer.consumerGroup(), "consumer's consumerGroup must be defined");
		Assert.notNull(mqConsumer.topic(), "consumer's topic must be defined");
		Assert.notNull(mqConsumer.tag(), "consumer's tag must be defined");
		if (!AbstractMQPushConsumer.class.isAssignableFrom(bean.getClass())) {
			throw new RuntimeException(bean.getClass().getName() + " - consumer未实现Consumer抽象类");
		}

		// 配置push consumer
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(mqConsumer.consumerGroup());
		consumer.setNamesrvAddr(mqProperties.getNamesrv());
		consumer.setMessageModel(MessageModel.valueOf(mqConsumer.messageMode()));
		consumer.subscribe(mqConsumer.topic(), StringUtils.join(mqConsumer.tag(), "||"));
		AbstractMQPushConsumer abstractMQPushConsumer = (AbstractMQPushConsumer) bean;
		if (MessageExtConst.CONSUME_MODE_CONCURRENTLY.equals(mqConsumer.consumeMode())) {
			consumer.registerMessageListener((List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) ->
					abstractMQPushConsumer.dealMessage(list, consumeConcurrentlyContext));
		} else if (MessageExtConst.CONSUME_MODE_ORDERLY.equals(mqConsumer.consumeMode())) {
			consumer.registerMessageListener((List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) ->
					abstractMQPushConsumer.dealMessage(list, consumeOrderlyContext));
		} else {
			throw new RuntimeException("unknown consume mode ! only support CONCURRENTLY and ORDERLY");
		}
		abstractMQPushConsumer.setConsumer(consumer);
		consumer.start();
		log.info("consumer: {} started", consumer.getConsumerGroup());
	}

	private void initProducer(String beanName, Object bean) throws Exception {
		MQTransactionProducer producerListener = applicationContext.findAnnotationOnBean(beanName, MQTransactionProducer.class);
		if (StringUtils.isEmpty(mqProperties.getNamesrv())) {
			throw new RuntimeException("name server address must be defined");
		}
		Assert.notNull(producerListener.producerGroup(), "producer's producerGroup must be defined");
		Assert.notNull(producerListener.topic(), "producer's topic must be defined");
		Assert.notNull(producerListener.tag(), "producer's tag must be defined");
		if (!AbstractMQTransactionProducer.class.isAssignableFrom(bean.getClass())) {
			throw new RuntimeException(bean.getClass().getName() + " - Listener未实现AbstractMQTransactionProducer抽象类");
		}

		ExecutorService executorService = new ThreadPoolExecutor(5, 100, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName("rocketmq-transaction-message-thread" + mqProperties.getProducerGroup());
				return thread;
			}
		});

		// 事务消息的生产者实例，需要在启动前设置TransactionListener
		TransactionMQProducer transactionProducer = new TransactionMQProducer("Transaction" + mqProperties.getProducerGroup());
		transactionProducer.setNamesrvAddr(mqProperties.getNamesrv());
		transactionProducer.setExecutorService(executorService);
		transactionProducer.setTransactionListener((AbstractMQTransactionProducer) bean);
		transactionProducer.start();

		producerMap.put(producerListener.topic() + "_" + producerListener.tag(), transactionProducer);
		log.info("transaction producer: {} started", producerListener.producerGroup());
	}
}
