package com.mac.kafka;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.mac.kafka.consumer.ConsumerBuilder;
import com.mac.kafka.partitioner.CustomPartitioner;
import com.mac.kafka.producer.ProducerBuilder;

public class App {
	public static void main(String[] args) {
		producer();
		consumer();
	}

	static void consumer() {
		Consumer<Long, String> consumer = ConsumerBuilder.build(consumerProps(), Config.TOPIC_NAME);

		int messagesNotFound = 0;

		while (true) {
			final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
			if (consumerRecords.count() == 0) {
				messagesNotFound++;
				if (messagesNotFound > Config.MAX_NO_MESSAGE_FOUND_COUNT)
					break;
				else
					continue;
			}

			consumerRecords.forEach(record -> {
				System.out.println("Record Key " + record.key());
				System.out.println("Record value " + record.value());
				System.out.println("Record partition " + record.partition());
				System.out.println("Record offset " + record.offset());
			});
			consumer.commitAsync();
		}
		consumer.close();
	}

	static void producer() {
		Producer<Long, String> producer = ProducerBuilder.build(producerProps());

		for (int index = 0; index < Config.MESSAGE_COUNT; index++) {
			final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(Config.TOPIC_NAME,
					"This is record " + index);
			try {
				RecordMetadata metadata = producer.send(record).get();
				System.out.println("Record sent with key " + index + " to partition " + metadata.partition()
						+ " with offset " + metadata.offset());
			} catch (ExecutionException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			} catch (InterruptedException e) {
				System.out.println("Error in sending record");
				System.out.println(e);
			}
		}
	}

	private static Properties producerProps() {
		Properties props = new Properties();
		props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getCanonicalName());
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.KAFKA_BROKERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, Config.CLIENT_ID);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		return props;
	}

	private static Properties consumerProps() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.KAFKA_BROKERS);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Config.GROUP_ID_CONFIG);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Config.MAX_POLL_RECORDS);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Config.OFFSET_RESET_EARLIER);
		return props;
	}
}
