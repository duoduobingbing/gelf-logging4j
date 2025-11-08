package io.github.duoduobingbing.gelflogging4j.gelf.standalone;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfMessageBuilder;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSenderFactory;

/**
 * @author Mark Paluch
 */
class StandaloneGelfSenderTests {

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
    }

    @Test
    void testStandalone() throws Exception {
        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration(new Slf4jErrorReporter());

        configuration.setHost("test:standalone");
        configuration.setPort(12345);

        GelfSender sender = GelfSenderFactory.createSender(configuration);

        sender.sendMessage(GelfMessageBuilder.newInstance().withFullMessage("message").withFacility(null).build());

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);
        AssertJAssertions.assertThat(gelfMessage.getFullMessage()).isEqualTo("message");
        AssertJAssertions.assertThat(gelfMessage.toJson()).isEqualTo("{\"full_message\":\"message\"}");

        sender.close();

    }
}
