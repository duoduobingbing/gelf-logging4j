package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import com.google.common.collect.Lists;
import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.KafkaIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.PropertiesHelper;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
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
    final KafkaContainer kafkaContainer = KafkaIntegrationTestBase.provideKafkaContainer();
    //Below's code is used for setting a fixed port. Enable below code only for debugging purposes!
//            .withCreateContainerCmdModifier(
//                    cmd -> cmd
//                            .getHostConfig()
//                            .withPortBindings(
//                                    new PortBinding(Binding.bindPort(19092), new ExposedPort(9092))
//                            )
//            );

    @Test
    void testKafkaSender() throws Exception {

        final InputStream resolvedPropertiesStream = getPropertiesWithRealPortReplaced();

        LogManager.getLogManager().readConfiguration(resolvedPropertiesStream);

        Logger logger = Logger.getLogger(getClass().getName());

        String logMessage = "Log from kafka";

        logger.log(Level.INFO, logMessage);

        try (KafkaConsumer<String, String> consumer = KafkaIntegrationTestBase.createKafkaStringConsumer(kafkaContainer.getBootstrapServers());) {
            consumer.subscribe(Lists.newArrayList(KAFKA_LOG_TOPIC));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));
            AssertJAssertions.assertThat(records).isNotNull();
            AssertJAssertions.assertThat(records.isEmpty()).isFalse();
            AssertJAssertions.assertThat(records.count()).isEqualTo(1);

            String jsonValueAsString = records.iterator().next().value();
            JsonMapper jsonMapper = JsonUtil.createJsonMapper();
            JsonNode gelfMessageJsonNode = jsonMapper.readTree(jsonValueAsString);
            String fullMessage = gelfMessageJsonNode.at("/%s".formatted(GelfMessage.FIELD_FULL_MESSAGE)).stringValue();
            AssertJAssertions.assertThat(fullMessage).isEqualTo(logMessage);
        }

    }

    private InputStream getPropertiesWithRealPortReplaced() throws IOException {
        return PropertiesHelper.replacePortInResource("/jul/test-kafka-logging.properties", kafkaContainer, 9092, 19092);
    }
}
