package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.util.concurrent.TimeUnit;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ConstantBackOff}.
 *
 * @author Mark Paluch
 */
class ConstantBackOffUnitTests {

    @Test
    void shouldReturnConstantBackoff() {

        ConstantBackOff backOff = new ConstantBackOff(10, TimeUnit.SECONDS);

        AssertJAssertions.assertThat(backOff.start().nextBackOff()).isEqualTo(TimeUnit.SECONDS.toMillis(10));
    }

}
