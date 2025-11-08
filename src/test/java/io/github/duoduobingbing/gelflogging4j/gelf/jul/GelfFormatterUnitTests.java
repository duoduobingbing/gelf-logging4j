package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.LogMessageField;

/**
 * @author Greg Peterson
 */
class GelfFormatterUnitTests {

    private Logger logger;

    @AfterAll
    static void afterClass() {
        LogManager.getLogManager().reset();
    }

    @BeforeEach
    void before() throws Exception {
        TestHandler.clear();
        LogManager.getLogManager().readConfiguration(
                GelfFormatterUnitTests.class.getResourceAsStream("/jul/test-gelf-formatter.properties"));
        logger = Logger.getLogger(GelfFormatterUnitTests.class.getName());
    }

    @Test
    void test() {
        logger.info("test1");
        logger.info("test2");
        logger.info("test3");

        String[] loggedLines = TestHandler.getLoggedLines();
        AssertJAssertions.assertThat(loggedLines.length).isEqualTo(3);
        AssertJAssertions.assertThat(parseToJSONObject(loggedLines[0]).get("full_message")).isEqualTo("test1");
        AssertJAssertions.assertThat(parseToJSONObject(loggedLines[1]).get("full_message")).isEqualTo("test2");
        AssertJAssertions.assertThat(parseToJSONObject(loggedLines[2]).get("full_message")).isEqualTo("test3");
    }

    @Test
    void testDefaults() {

        logger.info("test1");
        logger.info("test2");
        logger.info("test3");

        Map<String, Object> message = getMessage();

        AssertJAssertions.assertThat(message.get("version")).isNull();
        AssertJAssertions.assertThat(message).containsEntry("full_message", "test1");
        AssertJAssertions.assertThat(message).containsEntry("short_message", "test1");
        AssertJAssertions.assertThat(message).containsEntry("facility", "gelf-logging4j");
        AssertJAssertions.assertThat(message).containsEntry("level", "6");
        AssertJAssertions.assertThat(message).containsEntry(LogMessageField.NamedLogField.SourceMethodName.name(), "testDefaults");
        AssertJAssertions.assertThat(message).containsEntry(LogMessageField.NamedLogField.SourceClassName.name(), getClass().getName());
        AssertJAssertions.assertThat(message).containsKeys("Thread", "timestamp", "MyTime");
    }

    @Test
    void testConfigured() throws Exception {
        LogManager.getLogManager().readConfiguration(
                GelfFormatterUnitTests.class.getResourceAsStream("/jul/test-gelf-formatter-configured.properties"));

        logger.info("test1");
        logger.info("test2");
        logger.info("test3");

        Map<String, Object> message = getMessage();

        AssertJAssertions.assertThat(message.get("version")).isNull();
        AssertJAssertions.assertThat(message).containsEntry("full_message", "test1");
        AssertJAssertions.assertThat(message).containsEntry("short_message", "test1");
        AssertJAssertions.assertThat(message).containsEntry("facility", "test");
        AssertJAssertions.assertThat(message).containsEntry("level", "6");

        AssertJAssertions.assertThat(message).doesNotContainKeys(
                "SourceLineNumber",
                "SourceMethodName",
                "SourceSimpleClassName",
                "SourceClassName"
        );

        AssertJAssertions.assertThat(message).containsEntry("fieldName1", "fieldValue1");
        AssertJAssertions.assertThat(message).containsEntry("LoggerName", getClass().getName());

        AssertJAssertions.assertThat(message).containsKeys("timestamp", "MyTime");
    }

    Map<String, Object> getMessage() {
        String s = TestHandler.getLoggedLines()[0];
        try {
            return JsonUtil.parseToMap(s);
        } catch (RuntimeException e) {
            System.out.println("Trying to parse: " + s);
            throw e;
        }
    }

    private Map<String, Object> parseToJSONObject(String value) {
        return JsonUtil.parseToMap(value);
    }
}
