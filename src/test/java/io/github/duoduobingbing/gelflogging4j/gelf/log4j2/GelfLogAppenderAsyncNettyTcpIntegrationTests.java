package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TimingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.RuntimeContainer;
import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.netty.NettyLocalServer;

import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Mark Paluch
 */
class GelfLogAppenderAsyncNettyTcpIntegrationTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;

    private static LoggerContext loggerContext;
    private static NettyLocalServer server = new NettyLocalServer(NioServerSocketChannel.class);

    @BeforeAll
    static void setupClass() throws Exception {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-async-netty-tcp.xml");
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
        server.run();
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        loggerContext.reconfigure();
        server.close();
    }

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
        server.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWithLocation() throws Exception {

        Logger logger = loggerContext.getLogger("async.location");

        logger.info(LOG_MESSAGE);

        waitForGelf();

        List<?> jsonValues = server.getJsonValues();
        assertThat(jsonValues).hasSize(1);

        Map<String, Object> jsonValue = (Map<String, Object>) jsonValues.getFirst();

        assertThat(jsonValue.get(GelfMessage.FIELD_HOST)).isEqualTo(RuntimeContainer.FQDN_HOSTNAME);
        assertThat(jsonValue.get("_server.simple")).isEqualTo(RuntimeContainer.HOSTNAME);
        assertThat(jsonValue.get("_server.fqdn")).isEqualTo(RuntimeContainer.FQDN_HOSTNAME);
        assertThat(jsonValue.get("_server")).isEqualTo(RuntimeContainer.FQDN_HOSTNAME);
        assertThat(jsonValue.get("_server.addr")).isEqualTo(RuntimeContainer.ADDRESS);

        assertThat(jsonValue.get("_className")).isEqualTo(getClass().getName());
        assertThat(jsonValue.get("_simpleClassName")).isEqualTo(getClass().getSimpleName());

        assertThat(jsonValue.get(GelfMessage.FIELD_FULL_MESSAGE)).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(jsonValue.get(GelfMessage.FIELD_SHORT_MESSAGE)).isEqualTo(EXPECTED_LOG_MESSAGE);

        assertThat(jsonValue.get("_level")).isEqualTo("INFO");
        assertThat(jsonValue.get(GelfMessage.FIELD_LEVEL)).isEqualTo("6");

        assertThat(jsonValue.get(GelfMessage.FIELD_FACILITY)).isEqualTo("gelf-logging4j");
        assertThat(jsonValue.get("_fieldName1")).isEqualTo("fieldValue1");
        assertThat(jsonValue.get("_fieldName2")).isEqualTo("fieldValue2");
    }

    private void waitForGelf() throws InterruptedException, TimeoutException, ExecutionException {

        TimingHelper.waitUntil(
                () -> !server.getJsonValues().isEmpty(),
                5,
                ChronoUnit.SECONDS
        );


    }

}
