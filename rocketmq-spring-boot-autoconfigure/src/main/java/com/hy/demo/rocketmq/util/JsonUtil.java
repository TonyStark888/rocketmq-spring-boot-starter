package com.hy.demo.rocketmq.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 工具类
 * @author Huang ying
 */
public class JsonUtil {

	private static Logger log = LoggerFactory.getLogger(com.hy.demo.rocketmq.util.JsonUtil.class);

	private static ObjectMapper objectMapper = new ObjectMapper();

	private static final JsonParser parse = null;
	static{
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	public static String toJson(Object obj) {
		if (null == obj) return null;
		String json = null;
		try {
			json =  objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("convert to json str failed:", e);
		}
		return json;
	}

	public static <T> T fromJson(String json, Class<T> classOfT) {
		if (null == json) return null;
		T t = null;
		try {
			t = objectMapper.readValue(json, classOfT);
		} catch (IOException e) {
			log.error("convert from json str failed:", e);
		}
		return t;
	}


	public static <T> T fromJson(String json, TypeReference typeOfT) {
		if (null == json) return null;
		T t = null;
		try {
			t = objectMapper.readValue(json, typeOfT);
		} catch (IOException e) {
			log.error("convert from json str failed:", e);
		}
		return t;
	}

	public static <T> T fromJson(Object obj, Class<T> classOfT) {
		if (null == obj) return null;
		return objectMapper.convertValue(obj, classOfT);
	}

	public static <T> T fromJson(Object obj, TypeReference typeOfT) {
		if (null == obj) return null;
		return objectMapper.convertValue(obj, typeOfT);
	}
}
