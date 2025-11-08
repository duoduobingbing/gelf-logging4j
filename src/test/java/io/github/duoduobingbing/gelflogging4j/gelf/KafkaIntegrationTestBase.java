package io.github.duoduobingbing.gelflogging4j.gelf;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.DockerFileUtil;
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
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;
import java.util.Properties;

@Testcontainers
public class KafkaIntegrationTestBase {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaIntegrationTestBase.class);

    /**
     * Provides an unstarted Kafka container to be used by other tests
     * @return KafkaContainer
     */
    protected static KafkaContainer provideKafkaContainer(){
        String kafkaDockerImage = DockerFileUtil.parseDockerImageFromClassPathFile("docker/Kafka.Dockerfile");
        logger.info("Using kafka docker image: {}", kafkaDockerImage);

        return new KafkaContainer(DockerImageName.parse(kafkaDockerImage))
                .withEnv("KAFKA_GROUP_MIN_SESSION_TIMEOUT_MS", "2000") //increase min timeout so, we can use 2s instead of the default 6s
                .withLogConsumer((output) -> logger.info(output.getUtf8StringWithoutLineEnding()));
    }

    /**
     * Create as String Consumer for Kafka.
     * Properties are taken from the original charinthe/kafka-junit KafkaHelper
     * @param bootstrapServers bootstrapServers
     * @return KafkaProducer
     */
    protected static KafkaProducer<byte[], byte[]> createKafkaByteProducer(String bootstrapServers) {
        Objects.requireNonNull(bootstrapServers);

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "10");
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-junit");
        producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "2000");

        ByteArraySerializer keySerializer = new ByteArraySerializer();
        ByteArraySerializer valueSerializer = new ByteArraySerializer();

        //Configure whether the serializer handles keys or values
        keySerializer.configure(PropertiesHelper.propertiesToMap(producerProps), true);
        valueSerializer.configure(PropertiesHelper.propertiesToMap(producerProps), false);

        KafkaProducer<byte[], byte[]> byteProducer = new KafkaProducer<>(producerProps, keySerializer, valueSerializer);
        return byteProducer;
    }

    /**
     * Create as String Consumer for Kafka.
     * Properties are taken from the original charinthe/kafka-junit KafkaHelper
     * @param bootstrapServers bootstrapServers
     * @return KafkaConsumer
     */
    protected static KafkaConsumer<String, String> createKafkaStringConsumer(String bootstrapServers) {
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

        //Configure whether the deserializer handles keys or values
        keyDeserializer.configure(PropertiesHelper.propertiesToMap(consumerProperperties), true);
        valueDeserializer.configure(PropertiesHelper.propertiesToMap(consumerProperperties), false);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperperties, keyDeserializer, valueDeserializer);
        return consumer;
    }

}
