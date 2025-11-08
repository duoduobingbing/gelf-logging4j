package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfMessageAssembler;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSenderConfiguration;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSenderFactory;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSenderProvider;

/**
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class GelfLogHandlerErrorsTests {

    private static final String THE_HOST = "the host";
    private static final LogRecord MESSAGE = new LogRecord(Level.INFO, "message");

    @Mock
    private ErrorManager errorManager;

    @Mock
    private GelfSenderProvider senderProvider;

    @Mock
    private GelfMessageAssembler assembler;

    private GelfLogHandler sut = new GelfLogHandler();

    @BeforeEach
    void before() throws Exception {

        GelfSenderFactory.removeAllAddedSenderProviders();
        GelfSenderFactory.addGelfSenderProvider(senderProvider);
        sut.setErrorManager(errorManager);
    }

    @AfterEach
    void after() {
        GelfSenderFactory.removeGelfSenderProvider(senderProvider);
        GelfSenderFactory.removeAllAddedSenderProviders();
    }

    @Test
    void testRuntimeExceptionOnCreateSender() throws Exception {

        sut.setGraylogHost(THE_HOST);

        Mockito.when(senderProvider.supports(ArgumentMatchers.anyString())).thenReturn(true);
        Mockito.when(senderProvider.create(ArgumentMatchers.any(GelfSenderConfiguration.class))).thenThrow(new IllegalStateException());

        sut.publish(MESSAGE);

        Mockito.verify(errorManager, Mockito.atLeast(1)).error(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(IllegalStateException.class),
                ArgumentMatchers.anyInt()
        );
    }

    @Test
    void testInvalidMessage() {

        sut.publish(MESSAGE);

        Mockito.verify(errorManager, Mockito.atLeast(1)).error(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.<Exception>isNull(),
                ArgumentMatchers.anyInt()
        );
    }

    @Test
    void testErrorOnSend() {

        sut.publish(MESSAGE);

        Mockito.verify(errorManager, Mockito.atLeast(1)).error(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.<Exception>isNull(),
                ArgumentMatchers.anyInt());
    }

}
