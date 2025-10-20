package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * Unit tests for {@link GelfLogHandler}.
 *
 * @author Ralf Thaenert
 * @author Mark Paluch
 */
class GelfLogHandlerMessageFormatTests {

    @BeforeEach
    public void beforeEach() {
        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().reset();
    }

    @ParameterizedTest
    @CsvSource({ "foo bar %s,     foo bar aaa", //
            "foo bar '%s',   foo bar 'aaa'", //
            "foo bar ''%s'', foo bar ''aaa''", //
            "foo bar {0},    foo bar aaa", //
            "%sdfsdfk#! {0}, %sdfsdfk#! aaa" //
    })
    void testMessageFormatting(String logMessage, String expectedMessage) {
        GelfLogHandler handler = new GelfLogHandler();
        handler.setGraylogHost("test:sender");
        handler.setOriginHost("test");

        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.log(Level.INFO, logMessage, new String[] { "aaa" });
        assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        assertThat(gelfMessage.getFullMessage()).isEqualTo(expectedMessage);
        assertThat(gelfMessage.getShortMessage()).isEqualTo(expectedMessage);
    }
}
