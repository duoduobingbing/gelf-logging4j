package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Mark Paluch
 */
class GelfLogAppenderPropagateExceptionsTests {

    private static final String LOG_MESSAGE = "foo bar test log message";

    private static LoggerContext loggerContext;

    @BeforeAll
    static void beforeAll() throws Exception {
        loggerContext = Configurator.initialize("GelfLogAppenderPropagateExceptionsTests",
                GelfLogAppenderPropagateExceptionsTests.class.getClassLoader(), "log4j2/log4j2-propagate-exceptions.xml");
    }

    @AfterAll
    static void afterAll() throws Exception {
        Configurator.shutdown(loggerContext);
    }

    @Test
    void shouldPropagateException() throws Exception {

        AssertJAssertions.assertThatThrownBy(() -> {
            Logger logger = loggerContext.getLogger("biz.exception");
            logger.info(LOG_MESSAGE);
        }).isInstanceOf(AppenderLoggingException.class);
    }

    @Test
    void shouldUseFailoverAppender() throws Exception {

        Logger logger = loggerContext.getLogger("biz.failover");
        logger.info(LOG_MESSAGE);

        ListAppender failoverList = getListAppender("failoverList");
        AssertJAssertions.assertThat(failoverList.getEvents()).hasSize(1);
    }

    @Test
    void shouldIgnoreException() throws Exception {

        Logger logger = loggerContext.getLogger("biz.ignore");
        logger.info(LOG_MESSAGE);

        ListAppender ignoreList = getListAppender("ignoreList");
        AssertJAssertions.assertThat(ignoreList.getEvents()).hasSize(1);
    }

    ListAppender getListAppender(String name) {
        return (ListAppender) loggerContext.getConfiguration().getAppenders().get(name);
    }

}
