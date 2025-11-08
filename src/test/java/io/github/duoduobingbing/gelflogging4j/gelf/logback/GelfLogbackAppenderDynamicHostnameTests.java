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
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @since 2013-10-07
 */
class GelfLogbackAppenderDynamicHostnameTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private LoggerContext lc;

    @BeforeEach
    void before() throws Exception {
        lc = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.setMDCAdapter(MDC.getMDCAdapter());

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-with-dynamic-originhost.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.remove("mdcField1");
    }

    @Test
    void testOriginHost() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String crossCheckHostName = gelfMessage.getAdditonalFields().get("crossCheckHostName");

        String json = gelfMessage.toJson();
        AssertJAssertions.assertThat(json).contains("\"_crossCheckHostName\":\"" + crossCheckHostName + "\"");
        AssertJAssertions.assertThat(json).contains("\"host\":\"" + crossCheckHostName + "\"");
    }
}
