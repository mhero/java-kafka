package com.mac.kafka.consumer;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class ConsumerBuilder {

	public static Consumer<Long, String> build(Properties props, String topic) {

		Consumer<Long, String> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList(topic));
		return consumer;
	}

}
