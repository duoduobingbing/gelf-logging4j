package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class GelfREDISSenderIntegrationTests {

    @Test
    void recursiveCallIsBlocked() throws Exception {
        TestGefRedisSender sut = new TestGefRedisSender();
        sut.sendMessage(new GelfMessage());

        Field field = GelfREDISSender.class.getDeclaredField("callers");
        field.setAccessible(true);
        Set<?> callers = (Set<?>) field.get(sut);
        assertThat(callers).isEmpty();
    }

    static class TestGefRedisSender extends GelfREDISSender {

        TestGefRedisSender() throws IOException {
            super(null, null, null);
        }

        @Override
        protected boolean sendMessage0(GelfMessage message) {

            // recursive call
            return super.sendMessage(message);
        }
    }
}
