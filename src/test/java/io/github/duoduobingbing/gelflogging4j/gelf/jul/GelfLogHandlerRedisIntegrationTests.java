package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import java.io.InputStream;
import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.github.duoduobingbing.gelflogging4j.gelf.RedisIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.GelfREDISSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.TestRedisGelfSenderProvider;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.PropertiesHelper;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.JUnitAssertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.Sockets;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.RedisGelfSenderProvider;
import io.github.duoduobingbing.gelflogging4j.gelf.standalone.DefaultGelfSenderConfiguration;
import org.slf4j.MDC;

/**
 * @author Mark Paluch
 * @author tktiki
 * @since 27.09.13 08:25
 */
class GelfLogHandlerRedisIntegrationTests extends RedisIntegrationTestBase {

    @BeforeEach
    void before() {
        Assumptions.assumeTrue(Sockets.isOpen("localhost", redisMasterResolvedPort));

        GelfTestSender.getMessages().clear();
        MDC.remove("mdcField1");

        jedisMaster.flushDB();
        jedisMaster.flushAll();
    }

    @Test
    void testStandalone() throws Exception {

        InputStream propertiesStream = PropertiesHelper.replacePortInResource(
                "/jul/test-redis-logging.properties",
                redisLocalMasterTestcontainer,
                redisLocalMasterPort,
                6479
        );


        LogManager.getLogManager().readConfiguration(propertiesStream);

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        logger.log(Level.INFO, expectedMessage);

        List<String> list = jedisMaster.lrange("list", 0, jedisMaster.llen("list"));
        AssertJAssertions.assertThat(list).hasSize(1);

        Map<String, Object> map = JsonUtil.parseToMap(list.get(0));

        AssertJAssertions.assertThat(map.get("full_message")).isEqualTo(expectedMessage);
        AssertJAssertions.assertThat(map.get("short_message")).isEqualTo(expectedMessage);
        AssertJAssertions.assertThat(map.get("fieldName1")).isEqualTo("fieldValue1");
    }

    public static class SavingTestRedisGelfSenderProvider extends TestRedisGelfSenderProvider {

        Integer lastResolvedPort;
        URI lastResolvedHost;

        @Override
        protected GelfREDISSender createSenderInternal(URI hostUri, int port, ErrorReporter errorReporter) {
            this.lastResolvedHost = hostUri;
            this.lastResolvedPort = port;
            return super.createSenderInternal(hostUri, port, errorReporter);
        }
    }

    @Test
    void testMinimalRedisUriIsUsingDefaultPort() throws Exception {
        int redisDefaultPort = 6379;
        String uri = "redis://localhost/#list";

        SavingTestRedisGelfSenderProvider provider = new SavingTestRedisGelfSenderProvider();
        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);
        try (GelfSender gelfSender = provider.create(configuration)) {
            JUnitAssertions.assertNotNull(gelfSender);
        }

        AssertJAssertions.assertThat(provider.lastResolvedHost).isEqualTo(URI.create(uri));
        AssertJAssertions.assertThat(provider.lastResolvedPort).isEqualTo(redisDefaultPort);
    }

    @Test
    void testRedisWithPortUri() throws Exception {

        String uri = "redis://localhost:%d/#list".formatted(redisMasterResolvedPort);

        RedisGelfSenderProvider provider = new RedisGelfSenderProvider();
        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);
        GelfSender gelfSender = provider.create(configuration);

        gelfSender.sendMessage(new GelfMessage());
        gelfSender.close();
    }

    @Test
    void uriWithoutHost() {

        String uri = "redis:///#list";

        final DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);

        AssertJAssertions.assertThatThrownBy(() -> new RedisGelfSenderProvider().create(configuration)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uriWithoutFragment() {

        String uri = "redis://host/";

        final DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);

        AssertJAssertions.assertThatThrownBy(() -> new RedisGelfSenderProvider().create(configuration)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uriWithoutFragment2() {

        String uri = "redis://host";

        final DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);

        AssertJAssertions.assertThatThrownBy(() -> new RedisGelfSenderProvider().create(configuration)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uriWithoutFragment3() {

        String uri = "redis://host#";

        final DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost(uri);

        AssertJAssertions.assertThatThrownBy(() -> new RedisGelfSenderProvider().create(configuration)).isInstanceOf(IllegalArgumentException.class);
    }

    static class LocalTestWrappingErrorReporter implements ErrorReporter {

        final ErrorReporter errorReporter;
        final List<Map.Entry<String, Exception>> reportedErrors = new CopyOnWriteArrayList<>();

        public LocalTestWrappingErrorReporter(ErrorReporter errorReporter) {
            this.errorReporter = errorReporter;
        }

        @Override
        public void reportError(String message, Exception e) {
            errorReporter.reportError(message, e);
            reportedErrors.add(new SimpleEntry<>(message, e));
        }
    }

    @Test
    void testRedisNotAvailable() throws Exception {

        //This should give false, because we expect 10.1.5.20:52454 to not have any redis available
        Assumptions.assumeFalse(Sockets.isOpen("10.1.5.20", 52454));

        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-redis-not-available.properties"));

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        //Get the handler and switch the errorReporter to something that records the errors
        Handler[] handlers = logger.getParent().getHandlers();
        TestErrorRecordingGelfLogHandler gelfLogHandler = Arrays.stream(handlers)
                .filter(x -> x instanceof TestErrorRecordingGelfLogHandler)
                .map(x -> (TestErrorRecordingGelfLogHandler) x)
                .findFirst()
                .orElseThrow();


        logger.log(Level.INFO, expectedMessage);

        AssertJAssertions.assertThat(gelfLogHandler.recordedErrors).hasSize(1);
        AssertJAssertions.assertThat(gelfLogHandler.recordedErrors.getFirst().exception()).hasMessage("Cannot send REDIS data with key URI list");

        //Reset afterwards
        gelfLogHandler.recordedErrors.clear();


    }
}
