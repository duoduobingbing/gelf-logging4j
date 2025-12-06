package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.LogMessageField.NamedLogField;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.net.URL;

/**
 * @author duoduobingbing
 */
class GelfLogbackAppenderPatternFieldNoAdditionalLogbackDefaultsTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private LoggerContext lc = null;

    @BeforeEach
    void before() throws Exception {
        lc = new LoggerContext();
        lc.setMDCAdapter(MDC.getMDCAdapter());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);

        URL xmlConfigFile = getClass().getResource("/logback/logback-gelf-with-pattern-fields-no-default-fields.xml");

        configurator.doConfigure(xmlConfigFile);

        GelfTestSender.getMessages().clear();
    }

    @Test
    void testDefaultFieldsAreAddedByDefault() throws Exception {

        Logger logger = lc.getLogger(getClass());

        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().getFirst();

        AssertJAssertions.assertThat(gelfMessage.getFullMessage()).isEqualTo(LOG_MESSAGE); //default field -> always added!
        AssertJAssertions.assertThat(gelfMessage.getField(NamedLogField.ThreadName.getFieldName())).isNull(); //additional default logback field
        AssertJAssertions.assertThat(gelfMessage.getField("levelName")).isEqualTo("INFO-TEST");
    }


}
