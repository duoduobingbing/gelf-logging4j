package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * @author Mark Paluch
 */
class GelfTCPSSLSenderUnitTests {

    @Test
    void shouldNotFailOnCloseWithoutConnect() throws IOException {

        GelfTCPSSLSender sender = new GelfTCPSSLSender("localhost", 80, 1, 1, 1, false, null, null);

        sender.close();
        sender.close();
    }
}
