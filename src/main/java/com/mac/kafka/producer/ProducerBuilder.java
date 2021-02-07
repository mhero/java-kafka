package com.mac.kafka.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

public class ProducerBuilder {

	public static Producer<Long, String> build(Properties props) {
		return new KafkaProducer<>(props);
	}
}