package io.github.duoduobingbing.gelflogging4j.gelf;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.PropertiesHelper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;
import java.util.Properties;

@Testcontainers
public class KafkaIntegrationTestBase {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaIntegrationTestBase.class);

    protected static ConfluentKafkaContainer provideKafkaContainer(){
        return new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.8"))
                .withEnv("KAFKA_GROUP_MIN_SESSION_TIMEOUT_MS", "2000")
                .withLogConsumer((output) -> logger.info(output.getUtf8StringWithoutLineEnding()));
    }

    protected static KafkaProducer<byte[], byte[]> createKafkaProducer(String bootstrapServers) {
        Objects.requireNonNull(bootstrapServers);

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "10");
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-junit");
        producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "2000");
        producerProps.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        ByteArraySerializer keySerializer = new ByteArraySerializer();
        ByteArraySerializer valueSerializer = new ByteArraySerializer();

        keySerializer.configure(PropertiesHelper.propertiesToMap(producerProps), true);
        valueSerializer.configure(PropertiesHelper.propertiesToMap(producerProps), false);

        KafkaProducer<byte[], byte[]> byteProducer = new KafkaProducer<>(producerProps, keySerializer, valueSerializer);
        return byteProducer;
    }

    protected static KafkaConsumer<String, String> createKafkaConsumer(String bootstrapServers) {
        Objects.requireNonNull(bootstrapServers);

        Properties consumerProperperties = new Properties();
        consumerProperperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperperties.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-junit-consumer");
        consumerProperperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProperperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
        consumerProperperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperperties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "100");
        consumerProperperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "2000");
        consumerProperperties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "200");
        consumerProperperties.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "100");

        StringDeserializer keyDeserializer = new StringDeserializer();
        StringDeserializer valueDeserializer = new StringDeserializer();

        keyDeserializer.configure(PropertiesHelper.propertiesToMap(consumerProperperties), true);
        valueDeserializer.configure(PropertiesHelper.propertiesToMap(consumerProperperties), false);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperperties, keyDeserializer, valueDeserializer);
        return consumer;
    }

}
