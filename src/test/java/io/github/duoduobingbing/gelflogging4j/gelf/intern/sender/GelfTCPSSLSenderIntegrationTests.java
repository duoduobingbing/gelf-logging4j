package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.IntegrationTestSslCertHelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.netty.NettyLocalServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @author TKtiki
 * @author duoduobingbing
 */
class GelfTCPSSLSenderIntegrationTests {

    private static NettyLocalServer server = new NettyLocalServer(NioServerSocketChannel.class);
    private static SSLContext sslContext;

    @BeforeAll
    static void setupClass() throws Exception {

        final String keyStorePassword = "changeit";
        final byte[] pkcs12Keystore = IntegrationTestSslCertHelper.generateKeystore(keyStorePassword);

        KeyStore keyStore = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);

        try(ByteArrayInputStream bais = new ByteArrayInputStream(pkcs12Keystore)) {
            keyStore.load(bais, keyStorePassword.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        final SslContext sslContext = SslContextBuilder.forServer(kmf).build();

        GelfTCPSSLSenderIntegrationTests.sslContext = SSLContext.getInstance("TLSv1.3");
        GelfTCPSSLSenderIntegrationTests.sslContext.init(new KeyManager[0], tmf.getTrustManagers(), null);

        server.run(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {

                ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                ch.pipeline().addLast(server.getHandler());
            }
        });
    }

    @Test
    void shouldSendTCPMessagesViaSsl() throws Exception {

        GelfTCPSSLSender tcpsslSender = new GelfTCPSSLSender("localhost", server.getPort(), 1000, 1000, 1, true,
                new ErrorReporter() {
                    @Override
                    public void reportError(String message, Exception e) {
                        System.out.println(message);
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                }, sslContext);

        tcpsslSender.connect();

        GelfMessage gelfMessage = new GelfMessage("hello", "world", 1234, "7");
        tcpsslSender.write(gelfMessage.toTCPBuffer());

        for (int i = 0; i < 100; i++) {
            if (!server.getJsonValues().isEmpty()) {
                continue;

            }
            Thread.sleep(100);
        }

        assertThat(server.getJsonValues()).isNotEmpty();

        tcpsslSender.close();
    }

    @AfterAll
    static void afterClass() throws Exception {
        server.close();
    }
}
