package com.hy.demo.app.controller;

import com.hy.demo.rocketmq.config.MQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: demo
 * @description: 测试Demo
 * @author: Huang Ying
 * @create: 2019-10-11 16:27
 **/
@RestController
public class DemoController {
	@Autowired
	MQProducer producer;

	@GetMapping("/hello")
	public void sendMQ() {
		try {
			producer.sendMessageInTransaction("Test","Hello","Hello World",System.currentTimeMillis() + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
