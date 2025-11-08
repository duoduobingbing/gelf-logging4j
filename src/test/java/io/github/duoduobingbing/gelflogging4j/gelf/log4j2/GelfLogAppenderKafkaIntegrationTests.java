package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import com.google.common.collect.Lists;
import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.KafkaIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.PropertiesHelper;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

/**
 * @author Rifat Döver
 * @author duoduobingbing
 */
@Testcontainers
class GelfLogAppenderKafkaIntegrationTests extends KafkaIntegrationTestBase {

    private static final String KAFKA_LOG_TOPIC = "kafka-log-topic";

    @Container
    final KafkaContainer kafkaContainer = KafkaIntegrationTestBase.provideKafkaContainer();
    //Fixes port to 9092: Enable below only for debug purposes
//            .withCreateContainerCmdModifier(
//                    cmd -> cmd
//                            .getHostConfig()
//                            .withPortBindings(
//                                    new PortBinding(Binding.bindPort(9092), new ExposedPort(9092))
//                            )
//            );

    @AfterEach
    void tearDown() {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    static Path tmpFile;

    @Test
    void testKafkaSender() throws Exception {

        //Create a new temporary properties file with the right port
        tmpFile = Files.createTempFile("log4j2-gelf-with-kafka.xml.", ".tmp");
        try (InputStream inputStream = getPropertiesWithRealPortReplaced()) {
            Files.write(tmpFile, inputStream.readAllBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }

        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, tmpFile.toString());
        //PropertiesUtil.getProperties().reload(); is now a no-op.

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        loggerContext.reconfigure();
        Logger logger = loggerContext.getLogger(getClass().getName());

        String logMessage = "Log from kafka";

        logger.error(logMessage);

        try (KafkaConsumer<String, String> consumer = KafkaIntegrationTestBase.createKafkaStringConsumer(kafkaContainer.getBootstrapServers())) {
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

    @AfterAll
    static void cleanUp() {
        if (tmpFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(tmpFile);
        }catch (IOException e) {
            LoggerFactory.getLogger(GelfLogAppenderKafkaIntegrationTests.class).error("Failed to clean up temporary files", e);
        }
    }

    private InputStream getPropertiesWithRealPortReplaced() throws IOException {
        return PropertiesHelper.replacePortInResource("/log4j2/log4j2-gelf-with-kafka.xml", kafkaContainer, 9092, 9092);
    }
}
