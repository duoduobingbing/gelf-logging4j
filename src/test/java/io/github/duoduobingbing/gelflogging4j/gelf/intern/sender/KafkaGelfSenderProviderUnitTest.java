package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.util.stream.Stream;

import io.github.duoduobingbing.gelflogging4j.gelf.standalone.DefaultGelfSenderConfiguration;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Rifat Döver
 * @author duoduobingbing
 */
@ExtendWith(MockitoExtension.class)
class KafkaGelfSenderProviderUnitTest {

    private KafkaGelfSenderProvider kafkaSenderProvider = new KafkaGelfSenderProvider();

    static Stream<Arguments> testSupports() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("tcp"),
                Arguments.of("kafka")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSupports(String prefix) {
        AssertJAssertions
                .assertThat(kafkaSenderProvider.supports(prefix))
                .as("Prefix '" + prefix + "'")
                .isFalse();
    }

    @Test
    void testSupportsKafkaPrefix() {
        AssertJAssertions
                .assertThat(kafkaSenderProvider.supports("kafka:"))
                .isTrue();
    }

    @Test
    void testValidUri() {

        String host = "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#kafka-log-topic";

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(host);

        AssertJAssertions
                .assertThat(kafkaSenderProvider.create(configuration))
                .isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#",
            "kafka://localhost:9092,localhost:9093?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2"
    })
    void testUnspecifiedTopic(String host) {

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(host);

        AssertJAssertions
                .assertThatThrownBy(() -> kafkaSenderProvider.create(configuration))
                .hasMessage("Kafka URI must specify log topic as fragment.");
    }

    @Test
    void testUnspecifiedBroker() {

        String host = "kafka://?acks=1&ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks&retries=2#";

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(host);

        AssertJAssertions
                .assertThatThrownBy(() -> kafkaSenderProvider.create(configuration))
                .hasMessage("Kafka URI must specify bootstrap.servers.");
    }

    @Test
    void testValidPortNotSpecified() {
        String host = "kafka://localhost#topic";

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(host);

        AssertJAssertions.assertThat(kafkaSenderProvider.create(configuration)).isNotNull();
    }

    @Test
    void testValidPortSpecifiedInConfig() {
        String host = "kafka://localhost#topic";

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(host);
        configuration.setPort(9091);

        AssertJAssertions.assertThat(kafkaSenderProvider.create(configuration)).isNotNull();
    }
}
