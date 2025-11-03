package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import io.github.duoduobingbing.gelflogging4j.gelf.KafkaIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSenderConfiguration;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Map;

/**
 * @author Rifat Döver
 * @since 1.13
 */
@Testcontainers
@ExtendWith({MockitoExtension.class})
class KafkaGelfSenderProviderIntegrationTest extends KafkaIntegrationTestBase {

    private static final String TEST_LOG_TOPIC = "log-topic";

    private static final Logger logger = LoggerFactory.getLogger(KafkaGelfSenderProviderIntegrationTest.class);

    @Container
    KafkaContainer kafkaContainer = KafkaIntegrationTestBase.provideKafkaContainer();

    public record OnlyHostTestGelfSenderConfig(String host) implements GelfSenderConfiguration {

        @Override
        public String getHost(){
            return this.host;
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public ErrorReporter getErrorReporter() {
            return null;
        }

        @Override
        public Map<String, Object> getSpecificConfigurations() {
            return Map.of();
        }
    }

    @Test
    void testKafkaGelfSenderProvider() {

        String bootstrapServers = kafkaContainer.getBootstrapServers();

        StringBuilder builder = new StringBuilder();
        builder.append("kafka://");
        builder.append(bootstrapServers);
        builder.append("?request.timeout.ms=50&acks=all&client.id=kafka-junit&batch.size=10");
        builder.append("#");
        builder.append(TEST_LOG_TOPIC);

        GelfSenderConfiguration gelfSenderConfiguration = new OnlyHostTestGelfSenderConfig(builder.toString());
        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        KafkaGelfSenderProvider provider = new KafkaGelfSenderProvider();
        GelfSender sender = provider.create(gelfSenderConfiguration);
        AssertJAssertions.assertThat(sender).isNotNull();

        boolean success = sender.sendMessage(gelfMessage);

        AssertJAssertions.assertThat(success).isTrue();

        sender.close();
    }
}
