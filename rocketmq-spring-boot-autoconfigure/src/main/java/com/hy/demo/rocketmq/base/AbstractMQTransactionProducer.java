package com.hy.demo.rocketmq.base;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: rocketmq
 * @description: RocketMQ 事务消息生产端监听类
 * @author: Huang Ying
 * @create: 2019-10-11 14:24
 **/
public abstract class AbstractMQTransactionProducer<T> extends AbstractMQConsumer<T> implements TransactionListener {

	private static Logger log = LoggerFactory.getLogger(AbstractMQTransactionProducer.class);

	/**
	 * 执行本地事务，根据本地事务的执行结果，返回相应的LocalTransactionState状态
	 *
	 * @param message
	 * @param o
	 * @return
	 */
	@Override
	public LocalTransactionState executeLocalTransaction(Message message, Object o) {
		String msgKey = message.getKeys();
		log.info("start execute local transaction {}", msgKey);

		LocalTransactionState state = getLocalTransaction(o);

		log.info("execute local transaction: {},execute state: {}, current time: {}", msgKey, state, System.currentTimeMillis());
		return state;
	}

	/**
	 * 回查本地事务方法，适用于消息发送时返回UNKNOWN，事后补偿确认机制
	 * @param messageExt
	 * @return
	 */
	@Override
	public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
		String msgKey = messageExt.getKeys();
		log.info("start check local transaction {}", msgKey);

		T t = parseMessage(messageExt);
		LocalTransactionState state = checkLocalTransaction(t);

		log.info("execute check transaction: {},execute state: {}, current time: {}", msgKey, state, System.currentTimeMillis());
		return state;
	}

	/**
	 * 执行本地事务hook方法，所有事务消息发送端都需要实现特定的事务内容
	 * @param o 发送消息对象
	 * @return
	 */
	public abstract LocalTransactionState getLocalTransaction(Object o);

	/**
	 * 回查本地事务hook方法，所有事务消息发送端都需要实现，表示事务是否完成的标识
	 * @param t 发送消息对象
	 * @return
	 */
	public abstract LocalTransactionState checkLocalTransaction(T t);
}
