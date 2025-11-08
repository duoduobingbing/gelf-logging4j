package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import org.slf4j.MDC;

/**
 * @author Mark Paluch
 * @since 27.09.13 08:25
 */
class GelfLogHandlerTests {

    @BeforeEach
    void before() throws Exception {

        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/jul/test-logging.properties"));
        MDC.remove("mdcField1");
    }

    private void assertExpectedMessage(String expectedMessage) {
        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_1);
        AssertJAssertions.assertThat(gelfMessage.getField("MyTime")).isNotNull();
        AssertJAssertions.assertThat(gelfMessage.getFullMessage()).isEqualTo(expectedMessage);
        AssertJAssertions.assertThat(gelfMessage.getLevel()).isEqualTo("6");
        AssertJAssertions.assertThat(gelfMessage.getShortMessage()).isEqualTo(expectedMessage);
        AssertJAssertions.assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(8192);
    }

    @Test
    void testWithoutResourceBundle() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, expectedMessage, params);

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getField("MessageParam0")).isEqualTo("a");
        AssertJAssertions.assertThat(gelfMessage.getField("MessageParam1")).isEqualTo("b");

        assertExpectedMessage(expectedMessage);

    }

    @Test
    void testWithoutMessageParameters() throws Exception {

        LogManager.getLogManager().readConfiguration(
                getClass().getResourceAsStream("/jul/test-logging-without-message-parameters.properties"));

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, expectedMessage, params);

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getField("MessageParam0")).isNull();
        AssertJAssertions.assertThat(gelfMessage.getField("MessageParam1")).isNull();
    }

    @Test
    void testWithoutSourceLocation() throws Exception {

        LogManager.getLogManager().readConfiguration(
                getClass().getResourceAsStream("/jul/test-logging-without-location.properties"));

        Logger logger = Logger.getLogger(getClass().getName());
        String expectedMessage = "message1";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, expectedMessage, params);

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getField("SourceClassName")).isNull();
        AssertJAssertions.assertThat(gelfMessage.getField("SourceSimpleClassName")).isNull();
        AssertJAssertions.assertThat(gelfMessage.getField("SourceMethodName")).isNull();
        AssertJAssertions.assertThat(gelfMessage.getField("SourceLineNumber")).isNull();
    }

    @Test
    void testWithResourceBundleFormattingWithCurlyBrackets() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "params a and b";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, "message.format.curly.brackets", params);

        assertExpectedMessage(expectedMessage);
    }

    @Test
    void testWithResourceBundleFormattingWithoutParameters() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "no parameter supplied";

        logger.log(Level.INFO, "message.format.withoutParameter");

        assertExpectedMessage(expectedMessage);
    }

    @Test
    void testWithResourceBundleFormattingMalformed1() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "message.format.fail1";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, "message.format.fail1", params);

        assertExpectedMessage(expectedMessage);
    }

    @Test
    void testWithResourceBundleFormattingMalformed2() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "message.format.fail2";

        Object[] params = new Object[] { "a", "b", "c" };
        logger.log(Level.INFO, "message.format.fail2", params);

        assertExpectedMessage(expectedMessage);
    }

    @Test
    void testWithResourceBundleFormattingWithPercentages() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName(), "messages");
        String expectedMessage = "params a and 1";

        Object[] params = new Object[] { "a", 1, "c" };
        logger.log(Level.INFO, "message.format.percentages", params);

        assertExpectedMessage(expectedMessage);
    }

    @Test
    void testSimpleInfo() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.info(expectedMessage);
        assertExpectedMessage(expectedMessage);

    }

    @Test
    void testSimpleNull() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = null;
        logger.info(expectedMessage);

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).isEmpty();
    }

    @Test
    void testSimpleWarning() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.warning(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        AssertJAssertions.assertThat(gelfMessage.getLevel()).isEqualTo("4");

    }

    @Test
    void testSimpleSevere() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        String expectedMessage = "foo bar test log message";
        logger.severe(expectedMessage);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        AssertJAssertions.assertThat(gelfMessage.getLevel()).isEqualTo("3");

    }

    @Test
    void testNullMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.log(Level.INFO, null, new IllegalStateException());

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException");
        AssertJAssertions.assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException");
    }

    @Test
    void testEmptyMessageAndExceptionFallback() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.log(Level.INFO, "", new IllegalStateException("Help!"));

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getFullMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
        AssertJAssertions.assertThat(gelfMessage.getShortMessage()).isEqualTo("java.lang.IllegalStateException: Help!");
    }

    @Test
    void testEmptyMessage() throws Exception {
        Logger logger = Logger.getLogger(getClass().getName());

        logger.info("");

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).isEmpty();
    }

}
