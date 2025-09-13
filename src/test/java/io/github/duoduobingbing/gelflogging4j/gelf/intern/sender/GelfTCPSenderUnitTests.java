package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * Unit tests for {@link GelfTCPSender}.
 *
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class GelfTCPSenderUnitTests {

    @Mock
    private ErrorReporter errorReporter;

    @Captor
    private ArgumentCaptor<Exception> captor;

    @Test
    void connectionRefusedShouldReportException() throws Exception {

        GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", 65534, 100, 100, errorReporter);

        tcpSender.sendMessage(new GelfMessage());

        verify(errorReporter).reportError(anyString(), captor.capture());

        Exception exception = captor.getValue();
        assertThat(exception.getClass()).isEqualTo(IOException.class);
        assertThat(exception.getCause().getClass()).isEqualTo(ConnectException.class);
    }

    @Test
    void connectionTimeoutShouldReportException() throws Exception {

        GelfTCPSender tcpSender = new GelfTCPSender("8.8.8.8", 65534, 100, 100, errorReporter);

        tcpSender.sendMessage(new GelfMessage());

        verify(errorReporter).reportError(anyString(), captor.capture());

        Exception exception = captor.getValue();
        assertThat(exception.getClass()).isEqualTo(IOException.class);
        assertThat(exception.getCause().getClass()).isEqualTo(ConnectException.class);
    }

    @Test
    void connectionTimeoutShouldApply() throws Exception {

        long now = System.currentTimeMillis();
        GelfTCPSender tcpSender = new GelfTCPSender("8.8.8.8", 65534, 1000, 1000, errorReporter);
        tcpSender.sendMessage(new GelfMessage());

        long duration = System.currentTimeMillis() - now;
        assertThat(duration > 500).isTrue();
    }

    @Test
    void unknownHostShouldReportError() throws Exception {

        new GelfTCPSender("unknown.host.unknown", 65534, 100, 100, errorReporter);
        verify(errorReporter).reportError(anyString(), any(Exception.class));
    }

    @Test
    void shouldOpenConnection() throws Exception {

        int port = randomPort();

        ServerSocketChannel listener = ServerSocketChannel.open();
        listener.socket().bind(new InetSocketAddress(port));

        GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", port, 1000, 1000, errorReporter);

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        GelfTCPSender spy = spy(tcpSender);

        spy.sendMessage(gelfMessage);

        verify(spy, times(3)).isConnected();
        verify(spy).connect();

        listener.close();
        spy.close();
    }

    @Test
    void shouldSendDataToOpenPort() throws Exception {

        int port = randomPort();

        ServerSocketChannel listener = ServerSocketChannel.open();
        listener.socket().bind(new InetSocketAddress(port));

        GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", port, 1000, 1000, errorReporter);

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        tcpSender.sendMessage(gelfMessage);

        GelfTCPSender spy = spy(tcpSender);

        spy.sendMessage(gelfMessage);

        verify(spy, times(2)).isConnected();
        verify(spy, never()).connect();

        listener.close();
        spy.close();
    }

    @Test
    void shouldSendDataToClosedPort() throws Exception {

        int port = randomPort();

        ServerSocketChannel listener = ServerSocketChannel.open();
        listener.socket().bind(new InetSocketAddress(port));

        GelfTCPSender tcpSender = new GelfTCPSender("127.0.0.1", port, 1000, 1000, errorReporter);

        listener.socket().close();
        listener.close();

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");
        tcpSender.sendMessage(gelfMessage);

        GelfTCPSender spy = spy(tcpSender);

        spy.sendMessage(gelfMessage);

        verify(spy, times(2)).isConnected();
        verify(spy).connect();

        spy.close();
    }

    @Test
    void shouldSendHugeMessage() throws Exception {

        NoopGelfTCPSender tcpSender = new NoopGelfTCPSender("127.0.0.1", 1234, 1000, 1000, errorReporter);

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        for (int i = 0; i < 100; i++) {
            gelfMessage.addField(RandomStringUtils.random(1024), RandomStringUtils.random(1024));
        }

        tcpSender.sendMessage(gelfMessage);

        ByteBuffer buffer = tcpSender.buffer;
        assertThat(buffer.get()).isEqualTo((byte) '{');
        buffer.position(buffer.limit() - 2);
        assertThat(buffer.get()).isEqualTo((byte) '}');
        assertThat(buffer.get()).isEqualTo((byte) 0);
    }

    int randomPort() {
        Random random = new Random();
        return random.nextInt(50000) + 1024;
    }

    static class NoopGelfTCPSender extends GelfTCPSender {

        ByteBuffer buffer;

        NoopGelfTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter)
                throws IOException {
            super(host, port, connectTimeoutMs, readTimeoutMs, errorReporter);
        }

        @Override
        protected boolean isConnected() throws IOException {
            return true;
        }

        @Override
        protected void write(ByteBuffer buffer) throws IOException {
            this.buffer = buffer;
        }

    }

}
