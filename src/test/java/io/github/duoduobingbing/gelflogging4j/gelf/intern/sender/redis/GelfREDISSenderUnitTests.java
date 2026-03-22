package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;

/**
 * Unit tests for {@link GelfREDISSender}.
 *
 * @author Mark Paluch
 * @author duoduobingbing
 */
@ExtendWith(MockitoExtension.class)
class GelfREDISSenderUnitTests {

    @Mock
    RedisClientWrapper clientWrapper;

    @Mock
    ErrorReporter errorReporter;

    @Test
    void shouldCallCloseOnUnderlyingResource() {

        GelfREDISSender sender = new GelfREDISSender(clientWrapper, "key", errorReporter);

        sender.close();

        Mockito.verify(clientWrapper).close();
    }
}
