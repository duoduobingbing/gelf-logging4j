package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TimingHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class GelfTCPSenderIntegrationTests {

    private static final int PORT = 1234;
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    private final CountDownLatch latch = new CountDownLatch(1);
    private final Queue<Socket> sockets = new LinkedBlockingQueue<>();
    private final AtomicBoolean socketReady = new AtomicBoolean(false);
    private volatile ServerSocket serverSocket;
    private volatile boolean loopActive = true;
    private volatile boolean readFromServerSocket = true;

    private Thread thread;

    @BeforeEach
    void setUp() throws Exception {

        serverSocket = new ServerSocket(PORT);
        serverSocket.setSoTimeout(10000);

        thread = new Thread("GelfTCPSenderIntegrationTest-server") {

            @Override
            public void run() {

                while (loopActive) {
                    try {

                        Thread.sleep(0);

                        if (serverSocket.isClosed()) {
                            continue;

                        }

                        Socket socket = serverSocket.accept();
                        sockets.add(socket);
                        socket.setKeepAlive(true);
                        InputStream inputStream = socket.getInputStream();

                        while (!socket.isClosed()) {
                            socketReady.set(true);
                            if (readFromServerSocket) {
                                inputStream.transferTo(out);
                            }
                            Thread.sleep(1);

                            if (latch.getCount() == 0) {
                                socket.close();
                                socketReady.set(false);
                            }
                        }
                        socketReady.set(false);

                    } catch (IOException e) {
                        System.out.println("ReadIOException: " + e.getMessage());
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
    }

    @AfterEach
    void tearDown() throws IOException {

        thread.interrupt();

        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    void simpleTransport() throws Exception {

        thread.start();

        SmallBufferTCPSender sender = new SmallBufferTCPSender("localhost", PORT, 1000, 1000, new ErrorReporter() {
            @Override
            public void reportError(String message, Exception e) {
            }
        });

        GelfMessage gelfMessage = new GelfMessage("hello", StringUtils.repeat("hello", 100000), PORT, "7");
        ByteBuffer byteBuffer = gelfMessage.toTCPBuffer();
        int size = byteBuffer.remaining();

        sender.sendMessage(gelfMessage);
        sender.close();

        loopActive = false;
        latch.countDown();

        thread.join();

        assertThat(out.size()).isEqualTo(size);
    }

    @Test
    void shouldRecoverFromBrokenPipe() throws Exception {

        thread.start();

        SmallBufferTCPSender sender = new SmallBufferTCPSender("localhost", PORT, 1000, 1000, new ErrorReporter() {
            @Override
            public void reportError(String message, Exception e) {
                System.out.println(message + ": " + e.getClass().getName() + ": " + e.getMessage());
            }
        });

        GelfMessage gelfMessage = new GelfMessage("hello", StringUtils.repeat("hello", 100000), PORT, "7");
        ByteBuffer byteBuffer = gelfMessage.toTCPBuffer();

        assertThat(sender.sendMessage(gelfMessage)).isTrue();

        TimingHelper.waitUntil(() -> socketReady.get(),2L,ChronoUnit.SECONDS);

        sockets.poll().close();

        assertThat(sender.sendMessage(gelfMessage)).isTrue();

        sender.close();
    }

    @Test
    void shouldRecoverFromClosedPort() throws Exception {

        thread.start();

        SmallBufferTCPSender sender = new SmallBufferTCPSender("localhost", PORT, 1000, 1000, new ErrorReporter() {
            @Override
            public void reportError(String message, Exception e) {
                System.out.println(message + ": " + e.getClass().getName() + ": " + e.getMessage());
            }
        });

        GelfMessage gelfMessage = new GelfMessage("hello", StringUtils.repeat("hello", 100000), PORT, "7");
        ByteBuffer byteBuffer = gelfMessage.toTCPBuffer();

        assertThat(sender.sendMessage(gelfMessage)).isTrue();

        TimingHelper.waitUntil(() -> socketReady.get(),2L,ChronoUnit.SECONDS);

        sockets.poll().close();
        serverSocket.close();

        assertThat(sender.sendMessage(gelfMessage)).isFalse();

        serverSocket = new ServerSocket(PORT);

        TimingHelper.waitUntil(() -> !serverSocket.isClosed(), 1L, ChronoUnit.SECONDS);
        TimingHelper.waitUntil(socketReady::get, 1L, ChronoUnit.SECONDS);

        assertThat(sender.sendMessage(gelfMessage)).isTrue();

        sender.close();
    }

    @Test
    void sendToNonConsumingPort() throws Exception {

        serverSocket.setReceiveBufferSize(100);
        readFromServerSocket = false; // emulate read delays on a server side
        thread.start();
        final List<String> errors = new ArrayList<>();

        SmallBufferTCPSender sender = new SmallBufferTCPSender("localhost", PORT, 1000, 1000, new ErrorReporter() {
            @Override
            public void reportError(String message, Exception e) {
                errors.add(message);
            }
        });

        GelfMessage gelfMessage = new GelfMessage("hello", StringUtils.repeat("hello", 100000), PORT, "7");

        sender.sendMessage(gelfMessage);

        TimingHelper.waitUntil(() -> !errors.isEmpty(), 3, ChronoUnit.SECONDS);

        assertThat(errors).hasSize(1);
        assertThat(errors).containsOnly("Cannot write buffer to channel, no progress in writing");

        sender.close();
    }

    static class SmallBufferTCPSender extends GelfTCPSender {

        SmallBufferTCPSender(String host, int port, int connectTimeoutMs, int readTimeoutMs, ErrorReporter errorReporter)
                throws IOException {
            super(host, port, connectTimeoutMs, readTimeoutMs, errorReporter);
        }

        @Override
        protected SocketChannel createSocketChannel(int readTimeoutMs, boolean keepAlive) throws IOException {
            SocketChannel socketChannel = super.createSocketChannel(readTimeoutMs, keepAlive);

            socketChannel.socket().setSendBufferSize(100);

            return socketChannel;
        }
    }
}
