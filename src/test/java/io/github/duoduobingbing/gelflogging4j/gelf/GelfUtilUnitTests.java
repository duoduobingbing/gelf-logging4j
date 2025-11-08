package io.github.duoduobingbing.gelflogging4j.gelf;

import io.github.duoduobingbing.gelflogging4j.gelf.log4j2.Log4j2LogEvent;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class GelfUtilUnitTests {

    @Test
    void testProfilingString() throws Exception {

        GelfMessage message = new GelfMessage();

        MutableLogEvent mutableLogEvent = new MutableLogEvent();
        StringMap stringMap = new SortedArrayStringMap();
        stringMap.putValue(GelfUtil.MDC_REQUEST_START_MS, "" + (System.currentTimeMillis() - 12000));
        mutableLogEvent.setContextData(stringMap);

        Log4j2LogEvent log4j2LogEvent = new Log4j2LogEvent(mutableLogEvent);

        GelfUtil.addMdcProfiling(log4j2LogEvent, message);

        AssertJAssertions.assertThat(message.getAdditonalFields().get(GelfUtil.MDC_REQUEST_DURATION)).isEqualTo("12sec");
    }

    @Test
    void testProfilingLong() throws Exception {


        GelfMessage message = new GelfMessage();

        MutableLogEvent mutableLogEvent = new MutableLogEvent();
        StringMap stringMap = new SortedArrayStringMap();
        stringMap.putValue(GelfUtil.MDC_REQUEST_START_MS, (System.currentTimeMillis() - 12000));
        mutableLogEvent.setContextData(stringMap);

        Log4j2LogEvent log4j2LogEvent = new Log4j2LogEvent(mutableLogEvent);

        GelfUtil.addMdcProfiling(log4j2LogEvent, message);

        AssertJAssertions.assertThat(message.getAdditonalFields().get(GelfUtil.MDC_REQUEST_DURATION)).isEqualTo("12sec");
    }

}
