package biz.paluch.logging.gelf.test.helper;

/**
 * @author duoduobingbing
 */
public class TestAssertions {
    private TestAssertions() {}

    public static class JUnitAssertions extends org.junit.jupiter.api.Assertions {}

    public static class AssertJAssertions extends org.assertj.core.api.Assertions {}
}
