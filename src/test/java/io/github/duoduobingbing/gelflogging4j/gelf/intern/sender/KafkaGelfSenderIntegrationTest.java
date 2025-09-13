package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Properties;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaHelper;
import com.github.charithe.kafka.KafkaJunitExtension;
import com.github.charithe.kafka.KafkaJunitExtensionConfig;
import com.github.charithe.kafka.StartupMode;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

import com.google.common.collect.Lists;

/**
 * @author Rifat Döver
 */
@ExtendWith({ KafkaJunitExtension.class, MockitoExtension.class })
@KafkaJunitExtensionConfig(startupMode = StartupMode.WAIT_FOR_STARTUP)
class KafkaGelfSenderIntegrationTest {

    private static final String LOG_TOPIC = "test-log-topic";

    @Mock
    ErrorReporter errorReporter;
    private KafkaGelfSender kafkaGelfSender;

    @Test
    void testSend(EphemeralKafkaBroker kafkaBroker) {

        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        KafkaHelper kafkaHelper = KafkaHelper.createFor(kafkaBroker);
        Properties props = kafkaHelper.producerConfig();
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        KafkaProducer<byte[], byte[]> byteProducer = kafkaHelper.createByteProducer(props);

        kafkaGelfSender = new KafkaGelfSender(byteProducer, LOG_TOPIC, errorReporter);

        boolean success = kafkaGelfSender.sendMessage(gelfMessage);

        assertThat(success).isTrue();

        KafkaConsumer<String, String> consumer = kafkaHelper.createStringConsumer();
        consumer.subscribe(Lists.newArrayList(LOG_TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));

        assertThat(records).isNotNull();
        assertThat(records.isEmpty()).isFalse();
        assertThat(records.count()).isEqualTo(1);
        assertThat(records.iterator().next().value()).isEqualTo(gelfMessage.toJson());

        kafkaGelfSender.close();
    }
}
