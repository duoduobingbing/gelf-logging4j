package io.github.duoduobingbing.gelflogging4j.gelf.intern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfMessageAssembler;

/**
 * Unit tests for {@link GelfSenderFactory}.
 *
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class GelfSenderFactoryUnitTests {

    private static final String THE_HOST = "thehost";

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private GelfSender sender;

    @Mock
    private GelfSenderProvider senderProvider;

    @Mock
    private GelfMessageAssembler assembler;

    @BeforeEach
    void before() throws Exception {
        GelfSenderFactory.addGelfSenderProvider(senderProvider);

        when(assembler.getHost()).thenReturn(THE_HOST);
    }

    @AfterEach
    void after() throws Exception {
        GelfSenderFactory.removeGelfSenderProvider(senderProvider);
        GelfSenderFactory.removeAllAddedSenderProviders();
    }

    @Test
    void testCreateSender() throws Exception {

        when(assembler.getHost()).thenReturn(THE_HOST);
        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenReturn(sender);

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.EMPTY_MAP);

        assertThat(result).isSameAs(sender);
    }

    @Test
    void testCreateSenderFailUdp() throws Exception {

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNotNull();
        verify(errorReporter).reportError(anyString(), ArgumentMatchers.<Exception> any());
    }

    @Test
    void testCreateSenderFailTcp() throws Exception {

        reset(assembler);
        when(assembler.getHost()).thenReturn("tcp:" + THE_HOST);
        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNotNull();
        verify(errorReporter).reportError(anyString(), ArgumentMatchers.<Exception> any());
    }

    @Test
    void testCreateSenderFailUnknownHostException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new UnknownHostException());

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNull();

        verify(errorReporter).reportError(anyString(), any(UnknownHostException.class));

    }

    @Test
    void testCreateSenderFailSocketException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new SocketException());

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNull();

        verify(errorReporter).reportError(anyString(), any(SocketException.class));

    }

    @Test
    void testCreateSenderFailIOException() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new IOException());

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.EMPTY_MAP);
        assertThat(result).isNull();

        verify(errorReporter).reportError(anyString(), any(IOException.class));

    }

    @Test
    void testCreateSenderFailNPE() throws Exception {

        mockSupports();
        when(senderProvider.create(any(GelfSenderConfiguration.class))).thenThrow(new NullPointerException());

        try {
            GelfSenderFactory.createSender(assembler, errorReporter, new HashMap<String, Object>());
            fail("Missing NullPointerException");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    private void mockSupports() {
        when(senderProvider.supports(THE_HOST)).thenReturn(true);
    }
}
