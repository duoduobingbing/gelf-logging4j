package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.google.common.collect.Lists;
import io.github.duoduobingbing.gelflogging4j.gelf.KafkaIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Rifat Döver
 * @author duoduobingbing
 */
@Testcontainers
class GelfLogHandlerKafkaIntegrationTests extends KafkaIntegrationTestBase {

    private static final String KAFKA_LOG_TOPIC = "kafka-log-topic";

    @Container
    final ConfluentKafkaContainer kafkaContainer = KafkaIntegrationTestBase.provideKafkaContainer()
            .withCreateContainerCmdModifier(
                    cmd -> cmd
                            .getHostConfig()
                            .withPortBindings(
                                    new PortBinding(Binding.bindPort(19092), new ExposedPort(9092)) //TODO: make non fixed port
                            )
            );

    @Test
    void testKafkaSender() throws Exception {

        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/jul/test-kafka-logging.properties"));

        Logger logger = Logger.getLogger(getClass().getName());

        String logMessage = "Log from kafka";

        logger.log(Level.INFO, logMessage);

        try (KafkaConsumer<String, String> consumer = KafkaIntegrationTestBase.createKafkaConsumer(kafkaContainer.getBootstrapServers());) {
            consumer.subscribe(Lists.newArrayList(KAFKA_LOG_TOPIC));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));
            AssertJAssertions.assertThat(records).isNotNull();
            AssertJAssertions.assertThat(records.isEmpty()).isFalse();
            AssertJAssertions.assertThat(records.count()).isEqualTo(1);

            String jsonValueAsString = records.iterator().next().value();
            JsonMapper jsonMapper = getJsonMapper();
            JsonNode gelfMessageJsonNode = jsonMapper.readTree(jsonValueAsString);
            String fullMessage = gelfMessageJsonNode.at("/%s".formatted(GelfMessage.FIELD_FULL_MESSAGE)).stringValue();
            AssertJAssertions.assertThat(fullMessage).isEqualTo(logMessage);
        }

    }

    static JsonMapper getJsonMapper() {
        return JsonMapper
                .builder(
                        JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build()
                )
                .build();
    }
}
