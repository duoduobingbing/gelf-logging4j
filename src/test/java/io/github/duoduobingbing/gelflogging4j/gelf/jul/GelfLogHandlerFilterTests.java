package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import org.slf4j.MDC;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
class GelfLogHandlerFilterTests {

    @BeforeEach
    void before() throws Exception {

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-logging-with-filter.properties"));
        MDC.remove("mdcField1");
    }

    @AfterEach
    void after() throws Exception {
        LogManager.getLogManager().reset();

    }

    @Test
    void testSimpleInfo() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.info(expectedMessage);
        assertThat(GelfTestSender.getMessages()).isEmpty();
    }
}
