package io.github.duoduobingbing.gelflogging4j.gelf.intern;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
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

        Mockito.when(assembler.getHost()).thenReturn(THE_HOST);
    }

    @AfterEach
    void after() throws Exception {
        GelfSenderFactory.removeGelfSenderProvider(senderProvider);
        GelfSenderFactory.removeAllAddedSenderProviders();
    }

    @Test
    void testCreateSender() throws Exception {

        Mockito.when(assembler.getHost()).thenReturn(THE_HOST);
        mockSupports();
        Mockito.when(senderProvider.create(ArgumentMatchers.any(GelfSenderConfiguration.class))).thenReturn(sender);

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.emptyMap());

        AssertJAssertions.assertThat(result).isSameAs(sender);
    }

    @Test
    void testCreateSenderFailUdp() throws Exception {

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.emptyMap());
        AssertJAssertions.assertThat(result).isNotNull();
        Mockito.verify(errorReporter).reportError(ArgumentMatchers.anyString(), ArgumentMatchers.<Exception>any());
    }

    @Test
    void testCreateSenderFailTcp() throws Exception {

        Mockito.reset(assembler);
        Mockito.when(assembler.getHost()).thenReturn("tcp:" + THE_HOST);
        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.emptyMap());
        AssertJAssertions.assertThat(result).isNotNull();
        Mockito.verify(errorReporter).reportError(ArgumentMatchers.anyString(), ArgumentMatchers.<Exception>any());
    }

    @Test
    void testCreateSenderFailUnknownHostException() throws Exception {

        mockSupports();
        Mockito.when(senderProvider.create(ArgumentMatchers.any(GelfSenderConfiguration.class))).thenThrow(new UnknownHostException());

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.emptyMap());
        AssertJAssertions.assertThat(result).isNull();

        Mockito.verify(errorReporter).reportError(ArgumentMatchers.anyString(), ArgumentMatchers.any(UnknownHostException.class));

    }

    @Test
    void testCreateSenderFailSocketException() throws Exception {

        mockSupports();
        Mockito.when(senderProvider.create(ArgumentMatchers.any(GelfSenderConfiguration.class))).thenThrow(new SocketException());

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.emptyMap());
        AssertJAssertions.assertThat(result).isNull();

        Mockito.verify(errorReporter).reportError(ArgumentMatchers.anyString(), ArgumentMatchers.any(SocketException.class));

    }

    @Test
    void testCreateSenderFailIOException() throws Exception {

        mockSupports();
        Mockito.when(senderProvider.create(ArgumentMatchers.any(GelfSenderConfiguration.class))).thenThrow(new IOException());

        GelfSender result = GelfSenderFactory.createSender(assembler, errorReporter, Collections.emptyMap());
        AssertJAssertions.assertThat(result).isNull();

        Mockito.verify(errorReporter).reportError(ArgumentMatchers.anyString(), ArgumentMatchers.any(IOException.class));

    }

    @Test
    void testCreateSenderFailNPE() throws Exception {

        mockSupports();
        Mockito.when(senderProvider.create(ArgumentMatchers.any(GelfSenderConfiguration.class))).thenThrow(new NullPointerException());

        AssertJAssertions.assertThatThrownBy( () -> GelfSenderFactory.createSender(assembler, errorReporter, new HashMap<String, Object>())).isInstanceOf(NullPointerException.class);
    }

    private void mockSupports() {
        Mockito.when(senderProvider.supports(THE_HOST)).thenReturn(true);
    }
}
