package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.net.DatagramSocket;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.PortHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * Unit tests for {@link GelfUDPSender}.
 *
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class GelfUDPSenderUnitTests {

    @Mock
    private ErrorReporter errorReporter;

    @Captor
    private ArgumentCaptor<Exception> captor;

    @Test
    void unreachablePacketsShouldBeDiscardedSilently() throws Exception {

        try(GelfUDPSender udpSender = new GelfUDPSender("127.0.0.1", 65534, errorReporter);) {

            udpSender.sendMessage(new GelfMessage());

            Mockito.verifyNoInteractions(errorReporter);
        }
    }

    @Test
    void unknownHostShouldThrowException() throws Exception {

        try(GelfUDPSender sender = new GelfUDPSender("unknown.host.unknown", 65534, errorReporter)) {
            Mockito.verify(errorReporter).reportError(ArgumentMatchers.anyString(), ArgumentMatchers.any(Exception.class));
        }
    }

    @Test
    void shouldSendDataToOpenPort() throws Exception {

        int port = PortHelper.findAvailableTcpPort(1100, 51024);

        DatagramSocket socket = new DatagramSocket(port);

        GelfUDPSender udpSender = new GelfUDPSender("127.0.0.1", port, errorReporter);

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        udpSender.sendMessage(gelfMessage);

        GelfUDPSender spy = Mockito.spy(udpSender);

        spy.sendMessage(gelfMessage);

        Mockito.verify(spy).isConnected();
        Mockito.verify(spy, Mockito.never()).connect();

        socket.close();
        spy.close();
    }

    @Test
    void shouldSendDataToClosedPort() throws Exception {

        int port = PortHelper.findAvailableTcpPort(1100, 51024);

        DatagramSocket socket = new DatagramSocket(port);

        GelfUDPSender udpSender = new GelfUDPSender("127.0.0.1", port, errorReporter);
        socket.close();

        GelfMessage gelfMessage = new GelfMessage("short", "long", 1, "info");
        gelfMessage.setHost("host");

        udpSender.sendMessage(gelfMessage);

        GelfUDPSender spy = Mockito.spy(udpSender);
        Mockito.doReturn(true).when(spy).isConnected();

        spy.sendMessage(gelfMessage);

        Mockito.verify(spy).isConnected();
        Mockito.verify(spy, Mockito.never()).connect();

        spy.close();
    }

}
