package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.io.IOException;
import java.util.Set;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 * @author duoduobingbing
 */
class GelfREDISSenderIntegrationTests {

    @Test
    void recursiveCallIsBlocked() throws Exception {
        TestGefRedisSender sut = new TestGefRedisSender();
        sut.sendMessage(new GelfMessage());
        Set<?> callers = sut.getCallers();
        AssertJAssertions.assertThat(callers).isEmpty();
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

        Set<Thread> getCallers(){
            return this.callers;
        }
    }
}
