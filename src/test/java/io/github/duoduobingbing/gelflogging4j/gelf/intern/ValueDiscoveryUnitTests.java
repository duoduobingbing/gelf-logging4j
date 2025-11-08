package io.github.duoduobingbing.gelflogging4j.gelf.intern;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ValueDiscovery.Result;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Unit tests for {@link ValueDiscovery}.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @author Wolfgang Jung
 */
class ValueDiscoveryUnitTests {

    @Test
    void emptyString() {
        AssertJAssertions.assertThat(ValueDiscovery.discover("")).isEqualTo(Result.STRING);
    }

    static Stream<Arguments> singleDigitNumbers() {
        return IntStream.range(0, 10).mapToObj((i) -> Arguments.of(String.valueOf(i)));
    }

    @ParameterizedTest
    @MethodSource
    void singleDigitNumbers(String i) {
        AssertJAssertions.assertThat(ValueDiscovery.discover(i)).isEqualTo(Result.LONG);

    }

    @Test
    void twoDigitNumbers() {

        for (int i = 10; i < 99; i++) {
            AssertJAssertions.assertThat(ValueDiscovery.discover("" + i)).isEqualTo(Result.LONG);
        }
    }

    @Test
    void positiveAndNegativeDigitNumbers() {

        for (int i = 0; i < 99; i++) {
            AssertJAssertions.assertThat(ValueDiscovery.discover("-" + i)).isEqualTo(Result.LONG);
            AssertJAssertions.assertThat(ValueDiscovery.discover("+" + i)).isEqualTo(Result.LONG);
        }
    }

    @Test
    void singleDigitDouble() {

        for (int i = 0; i < 10; i++) {
            AssertJAssertions.assertThat(ValueDiscovery.discover("" + i + ".0")).isEqualTo(Result.DOUBLE);
        }
    }

    @Test
    void twoDigitDouble() {

        for (int i = 10; i < 99; i++) {
            AssertJAssertions.assertThat(ValueDiscovery.discover("" + i + ".0")).isEqualTo(Result.DOUBLE);
        }
    }

    @Test
    void positiveAndNegativeDigitDouble() {

        for (int i = 0; i < 99; i++) {
            AssertJAssertions.assertThat(ValueDiscovery.discover("-" + i + ".0")).isEqualTo(Result.DOUBLE);
            AssertJAssertions.assertThat(ValueDiscovery.discover("+" + i + ".0")).isEqualTo(Result.DOUBLE);
        }
    }

    @Test
    void doubleWithChars() {

        AssertJAssertions.assertThat(ValueDiscovery.discover("2e5")).isEqualTo(Result.DOUBLE);
        AssertJAssertions.assertThat(ValueDiscovery.discover("2e5.1")).isEqualTo(Result.STRING);
        AssertJAssertions.assertThat(ValueDiscovery.discover("1.2.3")).isEqualTo(Result.STRING);
        AssertJAssertions.assertThat(ValueDiscovery.discover("A")).isEqualTo(Result.STRING);
        AssertJAssertions.assertThat(ValueDiscovery.discover("9.156013e-002")).isEqualTo(Result.DOUBLE);
        AssertJAssertions.assertThat(ValueDiscovery.discover("0x0.0000000000001P-1022")).isEqualTo(Result.DOUBLE);
        AssertJAssertions.assertThat(ValueDiscovery.discover("0x1.fffffffffffffP+1023")).isEqualTo(Result.DOUBLE);
    }

    @Test
    void shouldDiscoverStringExceedingLength() {
        AssertJAssertions.assertThat(ValueDiscovery.discover("1111111111111111111")).isEqualTo(Result.LONG);
        AssertJAssertions.assertThat(ValueDiscovery.discover("11111111111111111111")).isEqualTo(Result.STRING);
        AssertJAssertions.assertThat(ValueDiscovery.discover("111111111111111111111111111111111")).isEqualTo(Result.STRING);
    }

    @Test
    void testString() {
        AssertJAssertions.assertThat(ValueDiscovery.discover("deadbeef")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testHexString() {
        AssertJAssertions.assertThat(ValueDiscovery.discover("0xdeadbeef")).isEqualTo(Result.LONG);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0deadbeef", "0123"})
    void testStringWithLeadingZeroAndHex(String value) {
        AssertJAssertions.assertThat(ValueDiscovery.discover(value)).isEqualTo(ValueDiscovery.Result.STRING);
        AssertJAssertions.assertThat(ValueDiscovery.discover(value)).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testStringZeroAndP() {
        AssertJAssertions.assertThat(ValueDiscovery.discover("0p")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testStringZeroAndX() {
        AssertJAssertions.assertThat(ValueDiscovery.discover("0x")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    @Test
    void testStringZeroXP() {
        AssertJAssertions.assertThat(ValueDiscovery.discover("0xp")).isEqualTo(ValueDiscovery.Result.STRING);
    }

    static Stream<Arguments> testInfinity() {
        return Stream.of(
                Arguments.arguments("Infinity", ValueDiscovery.Result.DOUBLE),
                Arguments.arguments("+Infinity", ValueDiscovery.Result.DOUBLE),
                Arguments.arguments("-Infinity", ValueDiscovery.Result.DOUBLE),
                Arguments.arguments("Infinity1", ValueDiscovery.Result.STRING)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInfinity(String value, ValueDiscovery.Result result) {
        AssertJAssertions.assertThat(ValueDiscovery.discover(value)).isEqualTo(result);
    }

    static Stream<Arguments> testNaN() {
        return Stream.of(
                Arguments.arguments("NaN", ValueDiscovery.Result.DOUBLE),
                Arguments.arguments("+NaN", ValueDiscovery.Result.STRING),
                Arguments.arguments("-NaN", ValueDiscovery.Result.STRING),
                Arguments.arguments("NaN1", ValueDiscovery.Result.STRING)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNaN(String value, ValueDiscovery.Result result) {
        AssertJAssertions.assertThat(ValueDiscovery.discover(value)).isEqualTo(result);
    }

}
