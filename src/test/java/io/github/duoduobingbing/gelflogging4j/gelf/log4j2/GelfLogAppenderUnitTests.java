package io.github.duoduobingbing.gelflogging4j.gelf.log4j2;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.MdcGelfMessageAssembler;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;
import org.mockito.Mockito;

/**
 * Unit tests for {@link GelfLogAppender}.
 * 
 * @author Mark Paluch
 */
class GelfLogAppenderUnitTests {

    @Test
    void testStop() {

        GelfSender sender = Mockito.mock(GelfSender.class);

        GelfLogAppender sut = new GelfLogAppender("name", null, new MdcGelfMessageAssembler(), true);
        sut.gelfSender = sender;

        sut.stop();

        Mockito.verify(sender).close();
    }

    @Test
    void testStopWithTimeout() {

        GelfSender sender = Mockito.mock(GelfSender.class);

        GelfLogAppender sut = new GelfLogAppender("name", null, new MdcGelfMessageAssembler(), true);
        sut.gelfSender = sender;

        sut.stop(0, TimeUnit.SECONDS);

        Mockito.verify(sender).close();
    }
}
