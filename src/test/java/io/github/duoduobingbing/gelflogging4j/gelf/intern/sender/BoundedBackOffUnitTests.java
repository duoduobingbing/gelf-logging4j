package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.util.concurrent.TimeUnit;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BoundedBackOff}.
 *
 * @author Mark Paluch
 */
class BoundedBackOffUnitTests {

    BoundedBackOff backOff = new BoundedBackOff(new ConstantBackOff(10, TimeUnit.SECONDS), 15, TimeUnit.SECONDS);

    @Test
    void shouldPassThruBackoff() {
        AssertJAssertions.assertThat(backOff.start().nextBackOff()).isEqualTo(TimeUnit.SECONDS.toMillis(10));
    }

    @Test
    void shouldCapBackoff() {

        BackOffExecution backOffExecution = backOff.start();

        AssertJAssertions.assertThat(backOffExecution.nextBackOff()).isEqualTo(TimeUnit.SECONDS.toMillis(10));
        AssertJAssertions.assertThat(backOffExecution.nextBackOff()).isEqualTo(TimeUnit.SECONDS.toMillis(10));
        AssertJAssertions.assertThat(backOffExecution.nextBackOff()).isEqualTo(BackOffExecution.STOP);
    }

}
