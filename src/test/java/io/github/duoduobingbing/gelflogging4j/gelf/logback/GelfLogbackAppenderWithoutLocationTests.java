package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import java.net.URL;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * @author Mark Paluch
 * @author duoduobingbing
 */
class GelfLogbackAppenderWithoutLocationTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private LoggerContext lc = null;

    @BeforeEach
    public void before() throws Exception {
        lc = new LoggerContext();
        lc.setMDCAdapter(MDC.getMDCAdapter());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-without-location.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.clear();
    }

    @Test
    void testWithoutLocation() {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getField("SourceClassName")).isNull();
        AssertJAssertions.assertThat(gelfMessage.getField("SourceSimpleClassName")).isNull();
        AssertJAssertions.assertThat(gelfMessage.getField("SourceMethodName")).isNull();
        AssertJAssertions.assertThat(gelfMessage.getField("SourceLineNumber")).isNull();
    }
}
