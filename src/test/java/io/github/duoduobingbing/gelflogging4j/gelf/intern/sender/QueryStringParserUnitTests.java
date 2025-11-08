package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;
import java.net.URI;
import java.util.Map;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;

/**
 * @author Mark Paluch
 */
class QueryStringParserUnitTests {

    @Test
    void testParse() throws Exception {
        Map<String, String> result = QueryStringParser.parse(URI.create("tcp:12345?KeY=value"));
        AssertJAssertions.assertThat(result).containsEntry("key", "value");
        AssertJAssertions.assertThat(result).doesNotContainEntry("KeY", "value");
    }

    @Test
    void getHost() throws Exception {
        AssertJAssertions.assertThat(QueryStringParser.getHost(URI.create("tcp:12345?KeY=value"))).isEqualTo("12345");
        AssertJAssertions.assertThat(QueryStringParser.getHost(URI.create("tcp:12345"))).isEqualTo("12345");
        AssertJAssertions.assertThat(QueryStringParser.getHost(URI.create("tcp://12345?KeY=value"))).isEqualTo("12345");
        AssertJAssertions.assertThat(QueryStringParser.getHost(URI.create("tcp://12345"))).isEqualTo("12345");
    }

    @Test
    void testGetTimeAsMsNoSuffix() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1000"));
        long result = QueryStringParser.getTimeAsMs(map, "timeout", -1);
        AssertJAssertions.assertThat(result).isEqualTo(1000);
    }

    @Test
    void testGetTimeAsMsNoSeconds() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1s"));
        long result = QueryStringParser.getTimeAsMs(map, "timeout", -1);
        AssertJAssertions.assertThat(result).isEqualTo(1000);
    }

    @Test
    void testGetTimeAsMsDefaultFallback() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1s"));
        long result = QueryStringParser.getTimeAsMs(map, "not here", -1);
        AssertJAssertions.assertThat(result).isEqualTo(-1);
    }

    @Test
    void testGetInt() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1000"));
        int result = QueryStringParser.getInt(map, "timeout", -1);
        AssertJAssertions.assertThat(result).isEqualTo(1000);
    }

    @Test
    void testGetIntDefault() throws Exception {
        Map<String, String> map = QueryStringParser.parse(URI.create("tcp:12345?timeout=1000"));
        int result = QueryStringParser.getInt(map, "not here", -1);
        AssertJAssertions.assertThat(result).isEqualTo(-1);
    }
}
