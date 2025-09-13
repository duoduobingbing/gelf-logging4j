package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

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
 * @author duoduobingbing
 * @since 2013-10-07
 */
class GelfLogbackAppenderHostnameTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private LoggerContext lc = null;

    @BeforeEach
    void before() throws Exception {
        lc = new LoggerContext();
        lc.setMDCAdapter(MDC.getMDCAdapter());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-with-host.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.remove("mdcField1");
    }

    @Test
    void testHost() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String json = gelfMessage.toJson();
        assertThat(json).contains("\"host\":\"1.2.3.4\"");
    }

    @Test
    void testOriginHost() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        String json = gelfMessage.toJson();
        assertThat(json).contains("\"_myOriginHost\":\"");
    }

}
