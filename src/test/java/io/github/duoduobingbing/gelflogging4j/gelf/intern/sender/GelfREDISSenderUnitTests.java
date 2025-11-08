package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;

/**
 * Unit tests for {@link GelfREDISSender}.
 *
 * @author Mark Paluch
 */
@ExtendWith(MockitoExtension.class)
class GelfREDISSenderUnitTests {

    @Mock
    Pool<Jedis> pool;

    @Mock
    ErrorReporter errorReporter;

    @Test
    void shouldClosePool() {

        GelfREDISSender sender = new GelfREDISSender(pool, "key", errorReporter);

        sender.close();

        Mockito.verify(pool).destroy();
    }
}
