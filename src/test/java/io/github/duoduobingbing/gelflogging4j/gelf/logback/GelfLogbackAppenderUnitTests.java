package io.github.duoduobingbing.gelflogging4j.gelf.logback;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.RuntimeContainer;

/**
 * @author Mark Paluch
 */
class GelfLogbackAppenderUnitTests {

    private static final String FACILITY = "facility";
    private static final String HOST = "host";
    private static final int GRAYLOG_PORT = 1;
    private static final int MAXIMUM_MESSAGE_SIZE = 1234;
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";

    @Test
    void testSameFieldsGelfLogbackAppender() {
        GelfLogbackAppender sut = new GelfLogbackAppender();

        sut.setExtractStackTrace("true");
        sut.setFacility(FACILITY);
        sut.setFilterStackTrace(true);
        sut.setGraylogHost(HOST);
        sut.setGraylogPort(GRAYLOG_PORT);
        sut.setMaximumMessageSize(MAXIMUM_MESSAGE_SIZE);
        sut.setDynamicMdcFields(".*");
        sut.setIncludeFullMdc(true);
        sut.setMdcProfiling(true);

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
        AssertJAssertions.assertThat(sut.isIncludeFullMdc()).isTrue();
        AssertJAssertions.assertThat(sut.isMdcProfiling()).isTrue();
    }

    @Test
    void testInvalidPort() throws Exception {

        AssertJAssertions.assertThatThrownBy(() -> {
            GelfLogbackAppender sut = new GelfLogbackAppender();
            sut.setPort(-1);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testInvalidMaximumMessageSize() throws Exception {

        AssertJAssertions.assertThatThrownBy(() -> {
                GelfLogbackAppender sut = new GelfLogbackAppender();
                sut.setMaximumMessageSize(-1);

        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testInvalidVersion() throws Exception {

        AssertJAssertions.assertThatThrownBy(() -> {
                GelfLogbackAppender sut = new GelfLogbackAppender();
                sut.setVersion("7");
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
