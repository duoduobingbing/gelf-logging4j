package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
class GelfLogHandlerSystemPropertiesTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String PROPERTY1 = "myproperty";
    public static final String PROPERTY1_VALUE = "value of myproperty";

    private static final String PROPERTY2 = "otherproperty";
    public static final String PROPERTY2_VALUE = "value of otherproperty";

    @BeforeEach
    void before() throws Exception {

        System.clearProperty(PROPERTY1);
        System.clearProperty(PROPERTY2);

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager()
                .readConfiguration(getClass().getResourceAsStream("/jul/test-logging-systemproperties.properties"));
    }

    @Test
    void testDefaults() throws Exception {

        Logger logger = Logger.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getField("propertyField1")).isEqualTo("${user.language}");
        AssertJAssertions.assertThat(gelfMessage.getField("propertyField2")).isEqualTo("${myproperty}");
        AssertJAssertions.assertThat(gelfMessage.getField("propertyField3")).isEqualTo("${otherproperty:fallback}");
        AssertJAssertions.assertThat(gelfMessage.getField("propertyField4")).isEqualTo("embedded${myproperty}property");
    }

}
