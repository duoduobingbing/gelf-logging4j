package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.netty.NettyLocalHTTPServer;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Aleksandar Stojadinovic
 * @author Patrick Brueckner
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class GelfHTTPSenderIntegrationTests {

    private static final GelfMessage GELF_MESSAGE = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");
    private NettyLocalHTTPServer server;
    private GelfHTTPSender sender;

    @Mock
    ErrorReporter errorReporter;

    @BeforeEach
    void setUp() throws Exception {
        server = new NettyLocalHTTPServer();
        server.run();

        sender = new GelfHTTPSender(
                URI.create("http://127.0.0.1:19393").toURL(),
                1000,
                1000,
                (message, e) -> {

                    System.out.println(message);

                    if (e != null) {
                        e.printStackTrace();
                    }
                }
        );
    }

    @AfterEach
    void tearDown() {
        server.close();
        sender.close();
    }

    @Test
    void sendMessageTestWithAcceptedResponse() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        boolean success = sender.sendMessage(gelfMessage);

        AssertJAssertions.assertThat(success).isTrue();
        Mockito.verifyNoInteractions(errorReporter);

        List<Object> jsonValues = server.getJsonValues();
        AssertJAssertions.assertThat(jsonValues).hasSize(1);

        Map<String, Object> messageJson = (Map<String, Object>) jsonValues.getFirst();
        AssertJAssertions.assertThat(messageJson.get("short_message")).isEqualTo(gelfMessage.getShortMessage());
        AssertJAssertions.assertThat(messageJson.get("full_message")).isEqualTo(gelfMessage.getFullMessage());
        AssertJAssertions.assertThat(messageJson.get("timestamp")).isEqualTo(gelfMessage.getTimestamp());
        AssertJAssertions.assertThat(messageJson.get("level")).isEqualTo(gelfMessage.getLevel());
    }

    @Test
    void sendMessageTestWithCreatedResponse() throws IOException {

        server.setReturnStatus(HttpResponseStatus.CREATED);

        GelfMessage gelfMessage = new GelfMessage("shortMessage", "fullMessage", 12121L, "WARNING");

        boolean success = sender.sendMessage(gelfMessage);

        AssertJAssertions.assertThat(success).isTrue();
        Mockito.verifyNoInteractions(errorReporter);

        List<Object> jsonValues = server.getJsonValues();
        AssertJAssertions.assertThat(jsonValues).hasSize(1);

        Map<String, Object> messageJson = (Map<String, Object>) jsonValues.getFirst();
        AssertJAssertions.assertThat(messageJson.get("short_message")).isEqualTo(gelfMessage.getShortMessage());
        AssertJAssertions.assertThat(messageJson.get("full_message")).isEqualTo(gelfMessage.getFullMessage());
        AssertJAssertions.assertThat(messageJson.get("timestamp")).isEqualTo(gelfMessage.getTimestamp());
        AssertJAssertions.assertThat(messageJson.get("level")).isEqualTo(gelfMessage.getLevel());
    }

    @Test
    void shouldUsePostHttpMethod() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        AssertJAssertions.assertThat(success).isTrue();
        AssertJAssertions.assertThat(server.getLastHttpRequest().name()).isEqualTo("POST");
    }

    @Test
    void shouldUseJsonContentType() throws IOException {

        server.setReturnStatus(HttpResponseStatus.ACCEPTED);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        AssertJAssertions.assertThat(success).isTrue();
        AssertJAssertions.assertThat(server.getLastHttpHeaders().get("Content-type")).isEqualTo("application/json");
    }

    @Test
    void sendMessageFailureTest() throws IOException {

        server.setReturnStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

        String uri = "http://127.0.0.1:19393";
        GelfHTTPSender sender = new GelfHTTPSender(URI.create(uri).toURL(), 1000, 1000, errorReporter);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        AssertJAssertions.assertThat(success).isFalse();
        Mockito.verify(errorReporter, Mockito.times(1)).reportError(ArgumentMatchers.anyString(), ArgumentMatchers.<Exception>isNull());

        try {
            sender.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    void sendMessageFailureUserInfoTest() throws IOException {

        server.setReturnStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

        String uri = "http://foo:(.*bar)@127.0.0.1:19394"; // unreachable host, and a regex
        GelfHTTPSender sender = new GelfHTTPSender(URI.create(uri).toURL(), 1000, 1000, errorReporter);

        boolean success = sender.sendMessage(GELF_MESSAGE);

        AssertJAssertions.assertThat(success).isFalse();
        Mockito
                .verify(errorReporter, Mockito.times(1))
                .reportError(
                        ArgumentMatchers.eq("Cannot send data to http://127.0.0.1:19394"), ArgumentMatchers.<Exception>isNotNull()
                );

        try {
            sender.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
