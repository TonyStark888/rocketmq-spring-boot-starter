package com.hy.demo.rocketmq.base;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * RocketMQ的消费者(Push模式)处理消息的接口
 */
public abstract class AbstractMQPushConsumer<T> extends AbstractMQConsumer<T> {
    private static Logger log = LoggerFactory.getLogger(AbstractMQPushConsumer.class);
    private DefaultMQPushConsumer consumer;

    public DefaultMQPushConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(DefaultMQPushConsumer consumer) {
        this.consumer = consumer;
    }

    public AbstractMQPushConsumer() {
    }

    /**
     * 继承这个方法处理消息
     * @see MessageExtConst
     * @param message 消息范型
     * @param extMap 存放消息附加属性的map, map中的key存放在 @link MessageExtConst 中
     * @return 处理结果
     */
    public abstract boolean process(T message, Map<String, Object> extMap);

    /**
     * 原生dealMessage方法，可以重写此方法自定义序列化和返回消费成功的相关逻辑
     *
     * @param list 消息列表
     * @param consumeConcurrentlyContext 上下文
     * @return 消费状态
     */
    public ConsumeConcurrentlyStatus dealMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        for(MessageExt messageExt : list) {
            log.debug("receive topic: {}, tags : {}" , messageExt.getTopic(), messageExt.getTags());
            // parse message body
            T t = parseMessage(messageExt);
            // parse ext properties
            Map<String, Object> ext = parseExtParam(messageExt);
            if( null != t && !process(t, ext)) {
                log.warn("consume fail , ask for re-consume topic: {}, tags : {}", messageExt.getTopic(), messageExt.getTags());
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
        return  ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 原生dealMessage方法，可以重写此方法自定义序列化和返回消费成功的相关逻辑
     *
     * @param list 消息列表
     * @param consumeOrderlyContext 上下文
     * @return 处理结果
     */
    public ConsumeOrderlyStatus dealMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
        for(MessageExt messageExt : list) {
            log.debug("receive topic: {}, tags : {}" , messageExt.getTopic(), messageExt.getTags());
            T t = parseMessage(messageExt);
            Map<String, Object> ext = parseExtParam(messageExt);
            if( null != t && !process(t, ext)) {
                log.warn("consume fail , ask for re-consume topic: {}, tags : {}", messageExt.getTopic(), messageExt.getTags());
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }
        return  ConsumeOrderlyStatus.SUCCESS;
    }
}
