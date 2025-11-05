package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.google.common.collect.Lists;
import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.KafkaIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.PropertiesHelper;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

/**
 * @author Rifat Döver
 * @author duoduobingbing
 */
@Testcontainers
class GelfLogbackAppenderKafkaIntegrationTests extends KafkaIntegrationTestBase {

    private static final String KAFKA_LOG_TOPIC = "log-topic";

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

        LoggerContext lc = new LoggerContext();
        lc.setMDCAdapter(MDC.getMDCAdapter());
        JoranConfigurator configurator = new JoranConfigurator();

        configurator.setContext(lc);

        InputStream xmlConfigFileStream = getPropertiesWithRealPortReplaced();

        configurator.doConfigure(xmlConfigFileStream);

        Logger testLogger = lc.getLogger("testLogger");

        String logMessage = "Log from kafka";
        testLogger.error(logMessage);

        try(KafkaConsumer<String, String> consumer = KafkaIntegrationTestBase.createKafkaStringConsumer(kafkaContainer.getBootstrapServers())) {
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
        return PropertiesHelper.replacePortInResource("/logback/logback-gelf-with-kafka.xml", kafkaContainer, 9092, 19092);
    }
}
