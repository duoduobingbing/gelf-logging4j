package biz.paluch.logging.gelf;

import biz.paluch.logging.gelf.log4j2.GelfDynamicMdcFieldType;
import biz.paluch.logging.gelf.test.helper.TestAssertions.JUnitAssertions;
import biz.paluch.logging.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * @author Thomas Herzog
 * @author duoduobingbing
 */
class GelfDynamicMdcFieldTypeUnitTests {

    public static Stream<Arguments> testWithInvalidValues() {
        return Stream.of(
                Arguments.argumentSet("regex is null", null, "String"),
                Arguments.argumentSet("type is null", ".*", null),
                Arguments.argumentSet("invalid regex", "*", "String")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testWithInvalidValues(String regex, String type) {

        // -- When --
        Executable executable = () -> GelfDynamicMdcFieldType.createField(regex, type);

        // -- Then --
        JUnitAssertions.assertThrows(IllegalArgumentException.class, executable);
    }


    @Test
    public void testWithValidRegexAndType() {
        // -- Given --
        String regex = ".*";
        String type = "String";

        // -- When --
        GelfDynamicMdcFieldType fieldType = GelfDynamicMdcFieldType.createField(regex, type);

        // -- Then --
        AssertJAssertions.assertThat(fieldType).isNotNull();
        AssertJAssertions.assertThat(fieldType.getPattern().pattern()).isEqualTo(regex);
        AssertJAssertions.assertThat(fieldType.getType()).isEqualTo(type);
    }
}
