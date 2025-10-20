package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * @author <a href="mailto:tobiassebastian.kaefer@1und1.de">Tobias Kaefer</a>
 * @author duoduobingbing
 * @since 2013-10-07
 */
class GelfLogbackAppenderTests extends AbstractGelfLogAppenderTests {

    @BeforeEach
    void before() throws Exception {
        lc = new LoggerContext();
        lc.setMDCAdapter(MDC.getMDCAdapter());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();

        MDC.remove("mdcField1");
    }
}
