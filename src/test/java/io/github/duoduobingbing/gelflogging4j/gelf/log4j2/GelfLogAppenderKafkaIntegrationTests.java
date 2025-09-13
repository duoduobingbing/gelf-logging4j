package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaHelper;
import com.google.common.collect.Lists;

/**
 * @author Rifat Döver
 */
@ExtendWith({ MockitoExtension.class })
class GelfLogAppenderKafkaIntegrationTests {

    private static final String KAFKA_LOG_TOPIC = "kafka-log-topic";

    @AfterEach
    void tearDown() {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        PropertiesUtil.getProperties().reload();
        ((LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    @Test
    void testKafkaSender() throws Exception {

        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-gelf-with-kafka.xml");
        PropertiesUtil.getProperties().reload();

        EphemeralKafkaBroker broker = EphemeralKafkaBroker.create(9092);
        KafkaHelper helper = KafkaHelper.createFor(broker);
        broker.start().get(30, TimeUnit.SECONDS);

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        PropertiesUtil.getProperties().reload();
        loggerContext.reconfigure();
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.error("Log from kafka");

        KafkaConsumer<String, String> consumer = helper.createStringConsumer();
        consumer.subscribe(Lists.newArrayList(KAFKA_LOG_TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(10000);

        assertThat(records).isNotNull();
        assertThat(records.isEmpty()).isFalse();
        assertThat(records.count()).isEqualTo(1);

        broker.stop();
    }
}
