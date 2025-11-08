package io.github.duoduobingbing.gelflogging4j.gelf.jul;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.RuntimeContainer;

/**
 * @author Mark Paluch
 */
class GelfLogHandlerUnitTests {

    private static final String FACILITY = "facility";
    private static final String HOST = "host";
    private static final int GRAYLOG_PORT = 1;
    private static final int MAXIMUM_MESSAGE_SIZE = 1234;
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Test
    void testSameFieldsGelfLogHandler() {
        GelfLogHandler sut = new GelfLogHandler();
        sut.setAdditionalFields("");
        sut.setExtractStackTrace("true");
        sut.setFacility(FACILITY);
        sut.setFilterStackTrace(true);
        sut.setGraylogHost(HOST);
        sut.setGraylogPort(GRAYLOG_PORT);
        sut.setMaximumMessageSize(MAXIMUM_MESSAGE_SIZE);
        sut.setTimestampPattern(TIMESTAMP_PATTERN);

        sut.flush();

        AssertJAssertions.assertThat(sut.getFacility()).isEqualTo(FACILITY);
        AssertJAssertions.assertThat(sut.getGraylogHost()).isEqualTo(HOST);
        AssertJAssertions.assertThat(sut.getHost()).isEqualTo(HOST);
        AssertJAssertions.assertThat(sut.getPort()).isEqualTo(GRAYLOG_PORT);
        AssertJAssertions.assertThat(sut.getGraylogPort()).isEqualTo(GRAYLOG_PORT);
        AssertJAssertions.assertThat(sut.getMaximumMessageSize()).isEqualTo(MAXIMUM_MESSAGE_SIZE);
        AssertJAssertions.assertThat(sut.getTimestampPattern()).isEqualTo(TIMESTAMP_PATTERN);
        AssertJAssertions.assertThat(sut.getOriginHost()).isEqualTo(RuntimeContainer.FQDN_HOSTNAME);

        AssertJAssertions.assertThat(sut.getExtractStackTrace()).isEqualTo("true");
        AssertJAssertions.assertThat(sut.isFilterStackTrace()).isTrue();
    }
}
