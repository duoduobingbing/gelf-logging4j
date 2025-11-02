package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import io.github.duoduobingbing.gelflogging4j.gelf.KafkaIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Rifat Döver
 * @author duoduobingbing
 */
@Testcontainers
class KafkaGelfSenderIntegrationTest extends KafkaIntegrationTestBase {

    private static final String LOG_TOPIC = "test-log-topic";

    private static final Logger logger = LoggerFactory.getLogger(KafkaGelfSenderIntegrationTest.class);

    private KafkaGelfSender kafkaGelfSender;

    @Container
    ConfluentKafkaContainer kafkaContainer = KafkaIntegrationTestBase.provideKafkaContainer();

    public static class TestLoggingErrorReporter implements ErrorReporter {

        @Override
        public void reportError(String message, Exception e) {
            logger.error("[TEST-ERROR] " + message, e);
        }
    }

    @Test
    void testSend() {

        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        String bootstrapServers = kafkaContainer.getBootstrapServers();

        KafkaProducer<byte[], byte[]> byteProducer = createKafkaProducer(bootstrapServers);

        kafkaGelfSender = new KafkaGelfSender(byteProducer, LOG_TOPIC, new TestLoggingErrorReporter());

        boolean success = kafkaGelfSender.sendMessage(gelfMessage);

        AssertJAssertions.assertThat(success).isTrue();

        try(KafkaConsumer<String, String> consumer = createKafkaConsumer(bootstrapServers)) {

            consumer.subscribe(new ArrayList<>(Collections.singleton(LOG_TOPIC)));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));

            AssertJAssertions.assertThat(records).isNotNull();
            AssertJAssertions.assertThat(records.isEmpty()).isFalse();
            AssertJAssertions.assertThat(records.count()).isEqualTo(1);
            AssertJAssertions.assertThat(records.iterator().next().value()).isEqualTo(gelfMessage.toJson());

            kafkaGelfSender.close();
        }
    }

}
