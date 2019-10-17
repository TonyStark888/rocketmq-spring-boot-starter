# spring boot starter for RocketMQ

### 项目介绍

[RocketMQ](https://github.com/apache/rocketmq) 是由阿里巴巴团队开发并捐赠给apache团队的优秀消息中间件，目前已经是apache的顶级项目之一，承受过历年双十一大促的考验。

你可以通过本项目轻松的集成Rocketmq到你的SpringBoot项目中。
本项目主要包含以下特性

* [x] 同步发送消息
* [x] 异步发送消息
* [x] 广播发送消息
* [x] 有序发送和消费消息
* [x] 发送延时消息
* [x] 消息tag和key支持
* [x] 自动序列化和反序列化消息体
* [x] 发送事务消息(NEW)
* [ ] ...

### 简单入门实例

#### 模块介绍：
rocketmq-spring-boot-starter：组件的依赖入口

rocketmq-spring-boot-autoconfigure： 自动配置模块

rocketmq-spring-boot-app：Demo示例，里面包含了最基本功能的用法

##### 1. 添加maven依赖：

```
<dependency>
	<groupId>com.hy.demo</groupId>
	<artifactId>rocketmq-spring-boot-starter</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```

##### 2. 添加配置：

```
spring:
  rocketmq:
  	#RocketMQ nameserver地址
    namesrv: 192.168.17.138:9876
    #生产者组名
    producerGroup: demo
    #订阅组名
    consumerGroup: demo
```
##### 3. 开始使用

消息生产者

```
#直接注入MQProducer类
@Autowired
MQProducer producer;
```

消息消费者
使用@MQConsumer注解
```
@MQConsumer(consumerGroup = "consumerTest", topic = "Test", tag = "Hello")
public class DemoConsumer extends AbstractMQPushConsumer<String> {
	@Override
	public boolean process(String message, Map<String, Object> extMap) {
		System.out.println(message);
		return true;
	}
}
```

事务消息生产者
新增类继承AbstractMQTransactionProducer，实现本地事务方法和回查事务方法，并使用@MQTransactionProducer注解
```
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
```

##### 4. 注意事项
创建事务消息时，禁止设置延迟时间，现提供的事务消息发送方法已不支持延迟时间的设置。