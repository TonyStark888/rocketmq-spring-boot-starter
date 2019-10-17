package com.hy.demo.rocketmq.base;

import com.hy.demo.rocketmq.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class MessageBuilder {
    private static Logger log = LoggerFactory.getLogger(MessageBuilder.class);

    private static final String[] DELAY_ARRAY = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h".split(" ");

    private String topic;
    private String tag;
    private Object message;
    private String key;
    private Integer delayTimeLevel;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static MessageBuilder of(String topic, String tag, Object message, String key) {
        MessageBuilder builder = new MessageBuilder();
        builder.setTopic(topic);
        builder.setTag(tag);
        builder.setMessage(message);
        builder.setKey(key);
        return builder;
    }

    public MessageBuilder delayTimeLevel(DelayTimeLevel delayTimeLevel) {
        this.delayTimeLevel = delayTimeLevel.getLevel();
        return this;
    }

    public Message build() {
        if(StringUtils.isEmpty(this.topic)) {
            throw new RuntimeException("no topic defined to send this message");
        }

        if (StringUtils.isEmpty(this.key)) {
            throw new RuntimeException("no message key defined to send this message");
        }

        if (null == this.message) {
            throw new RuntimeException("message body can not be null");
        }

        String str = JsonUtil.toJson(this.message);

        Message message = new Message(this.topic, str.getBytes(Charset.forName(RemotingHelper.DEFAULT_CHARSET)));
        if (!StringUtils.isEmpty(this.tag)) {
            message.setTags(this.tag);
        }

        message.setKeys(this.key);

        if(this.delayTimeLevel != null && this.delayTimeLevel > 0 && this.delayTimeLevel <= DELAY_ARRAY.length) {
            message.setDelayTimeLevel(this.delayTimeLevel);
        }
        return message;
    }



}
