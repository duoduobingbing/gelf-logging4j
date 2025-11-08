package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author Mark Paluch
 */
class GelfLogAppenderDynamicMdcTests {
    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String MDC_MY_MDC = "myMdc";
    private static final String MY_MDC_WITH_SUFFIX1 = "myMdc-with-suffix1";
    private static final String MY_MDC_WITH_SUFFIX2 = "myMdc-with-suffix2";
    private static final String VALUE_1 = "value";
    private static final String VALUE_2 = "value1";
    private static final String VALUE_3 = "value2";
    private static final String SOME_FIELD = "someField";
    private static final String SOME_OTHER_FIELD = "someOtherField";

    private static LoggerContext loggerContext;

    @BeforeAll
    static void setupClass() {
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2/log4j2-dynamic-mdc.xml");
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        loggerContext = (LoggerContext) LogManager.getContext(false);
        loggerContext.reconfigure();
    }

    @AfterAll
    static void afterClass() throws Exception {
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        //PropertiesUtil.getProperties().reload(); is now a no-op.
        loggerContext.reconfigure();
    }

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
        ThreadContext.clearAll();
    }

    @Test
    void testWithoutFields() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().getFirst();

        String myMdc = gelfMessage.getField(MDC_MY_MDC);
        AssertJAssertions.assertThat(myMdc).isNull();
    }

    @Test
    void testWithMdcPrefix() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(MDC_MY_MDC, VALUE_1);
        ThreadContext.put(MY_MDC_WITH_SUFFIX1, VALUE_2);
        ThreadContext.put(MY_MDC_WITH_SUFFIX2, VALUE_3);

        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().getFirst();

        AssertJAssertions.assertThat(gelfMessage.getField(MDC_MY_MDC)).isEqualTo(VALUE_1);
        AssertJAssertions.assertThat(gelfMessage.getField(MY_MDC_WITH_SUFFIX1)).isEqualTo(VALUE_2);
        AssertJAssertions.assertThat(gelfMessage.getField(MY_MDC_WITH_SUFFIX2)).isEqualTo(VALUE_3);
    }

    @Test
    void testWithMdcRegex() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put(SOME_FIELD, "included");
        ThreadContext.put(SOME_OTHER_FIELD, "excluded");

        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().getFirst();

        AssertJAssertions.assertThat(gelfMessage.getField(SOME_FIELD)).isEqualTo("included");
        AssertJAssertions.assertThat(gelfMessage.getField(SOME_OTHER_FIELD)).isNull();
    }

    @Test
    void testWithMdcFieldTypes() throws Exception {

        Logger logger = loggerContext.getLogger(getClass().getName());
        ThreadContext.put("myMdcs", "String");
        ThreadContext.put("myMdcl", "1");
        ThreadContext.put("myMdci", "2");
        ThreadContext.put("myMdcd", "2.1");
        ThreadContext.put("myMdcf", "2.2");

        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().getFirst();
        Map<String, Object> jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        AssertJAssertions.assertThat(jsonObject.get("myMdcs")).isEqualTo("String");
        AssertJAssertions.assertThat(jsonObject.get("myMdcl")).isEqualTo(1);
        AssertJAssertions.assertThat(jsonObject.get("myMdci")).isEqualTo(2);

        AssertJAssertions.assertThat(jsonObject.get("myMdcd")).isEqualTo(2.1);
        AssertJAssertions.assertThat(jsonObject.get("myMdcf")).isEqualTo(2.2);

        ThreadContext.put("myMdcl", "1.1");
        ThreadContext.put("myMdci", "2.1");
        ThreadContext.put("myMdcd", "wrong");
        ThreadContext.put("myMdcf", "wrong");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().getFirst();
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        AssertJAssertions.assertThat(jsonObject.get("myMdcl")).isEqualTo(1);
        AssertJAssertions.assertThat(jsonObject.get("myMdci")).isEqualTo(2);

        AssertJAssertions.assertThat(jsonObject.get("myMdcd")).isNull();
        AssertJAssertions.assertThat(jsonObject.get("myMdcf")).isEqualTo(0.0);

        ThreadContext.put("myMdcl", "b");
        ThreadContext.put("myMdci", "a");

        GelfTestSender.getMessages().clear();
        logger.info(LOG_MESSAGE);
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        gelfMessage = GelfTestSender.getMessages().getFirst();
        jsonObject = JsonUtil.parseToMap(gelfMessage.toJson(""));

        AssertJAssertions.assertThat(jsonObject.get("myMdcl")).isNull();
        AssertJAssertions.assertThat(jsonObject.get("myMdci")).isEqualTo(0);
    }
}
