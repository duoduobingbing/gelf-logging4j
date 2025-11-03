package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;


import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.google.common.collect.Lists;
import io.github.duoduobingbing.gelflogging4j.gelf.KafkaIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

/**
 * @author Rifat Döver
 * @author duoduobingbing
 */
@Testcontainers
class GelfLogAppenderKafkaIntegrationTests extends KafkaIntegrationTestBase {

    private static final String KAFKA_LOG_TOPIC = "kafka-log-topic";

    @Container
    final KafkaContainer kafkaContainer = KafkaIntegrationTestBase.provideKafkaContainer()
            .withCreateContainerCmdModifier(
                    cmd -> cmd
                            .getHostConfig()
                            .withPortBindings(
                                    new PortBinding(Binding.bindPort(9092), new ExposedPort(9092)) //TODO: make non fixed port
                            )
            );

    @AfterEach
    void tearDown() {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    @Test
    void testKafkaSender() throws Exception {

        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-gelf-with-kafka.xml");
        //PropertiesUtil.getProperties().reload(); is now a no-op.

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        loggerContext.reconfigure();
        Logger logger = loggerContext.getLogger(getClass().getName());

        String logMessage = "Log from kafka";

        logger.error(logMessage);

        try(KafkaConsumer<String, String> consumer = KafkaIntegrationTestBase.createKafkaStringConsumer(kafkaContainer.getBootstrapServers())) {
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
