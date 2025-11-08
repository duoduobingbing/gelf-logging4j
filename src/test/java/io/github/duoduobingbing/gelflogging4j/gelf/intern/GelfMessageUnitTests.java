package io.github.duoduobingbing.gelflogging4j.gelf.intern;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.StackTraceFilter;
import io.github.duoduobingbing.gelflogging4j.gelf.GelfMessageBuilder;

class GelfMessageUnitTests {

    private static final String FACILITY = "facility";
    private static final String VERSION = "2.0";
    private static final String FULL_MESSAGE = "full";
    private static final String SHORT_MESSAGE = "short";
    private static final String HOST = "host";
    private static final String LEVEL = "5";
    private static final long TIMESTAMP = 42;
    private static final int MESSAGE_SIZE = 5344;

    private static final Map<String, String> ADDITIONAL_FIELDS = new HashMap<>(
            Map.ofEntries(
                    Map.entry("a", "b"),
                    Map.entry("doubleNoDecimals", "2.0"),
                    Map.entry("doubleWithDecimals", "2.1"),
                    Map.entry("int", "2"),
                    Map.entry(
                            "exception1",
                            StackTraceFilter.getFilteredStackTrace(new IOException(new Exception(new Exception())))
                    ),
                    Map.entry(
                            "exception2",
                            StackTraceFilter.getFilteredStackTrace(new IllegalStateException(new Exception(new Exception())))
                    )
            )
    );

    @Test
    void testBuilder() {
        GelfMessage gelfMessage = buildGelfMessage();

        AssertJAssertions.assertThat(gelfMessage.getFacility()).isEqualTo(FACILITY);
        AssertJAssertions.assertThat(gelfMessage.getField("a")).isEqualTo("b");
        AssertJAssertions.assertThat(gelfMessage.getFullMessage()).isEqualTo(FULL_MESSAGE);
        AssertJAssertions.assertThat(gelfMessage.getHost()).isEqualTo(HOST);
        AssertJAssertions.assertThat(gelfMessage.getLevel()).isEqualTo(LEVEL);
        AssertJAssertions.assertThat(gelfMessage.getShortMessage()).isEqualTo(SHORT_MESSAGE);
        AssertJAssertions.assertThat(gelfMessage.getJavaTimestamp().longValue()).isEqualTo(TIMESTAMP);
        AssertJAssertions.assertThat(gelfMessage.getVersion()).isEqualTo(VERSION);
        AssertJAssertions.assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(MESSAGE_SIZE);

    }

    @Test
    void testGelfMessage() {
        GelfMessage gelfMessage = createGelfMessageWithFixedMsgId();

        AssertJAssertions.assertThat(gelfMessage.getFacility()).isEqualTo(FACILITY);
        AssertJAssertions.assertThat(gelfMessage.getField("a")).isEqualTo("b");
        AssertJAssertions.assertThat(gelfMessage.getFullMessage()).isEqualTo(FULL_MESSAGE);
        AssertJAssertions.assertThat(gelfMessage.getHost()).isEqualTo(HOST);
        AssertJAssertions.assertThat(gelfMessage.getLevel()).isEqualTo(LEVEL);
        AssertJAssertions.assertThat(gelfMessage.getShortMessage()).isEqualTo(SHORT_MESSAGE);
        AssertJAssertions.assertThat(gelfMessage.getJavaTimestamp().longValue()).isEqualTo(TIMESTAMP);
        AssertJAssertions.assertThat(gelfMessage.getVersion()).isEqualTo(VERSION);
        AssertJAssertions.assertThat(gelfMessage.getMaximumMessageSize()).isEqualTo(MESSAGE_SIZE);

        AssertJAssertions.assertThat(gelfMessage.toJson()).contains("\"_int\":2");
        AssertJAssertions.assertThat(gelfMessage.toJson()).contains("\"_doubleNoDecimals\":2.0");
        AssertJAssertions.assertThat(gelfMessage.toJson()).contains("\"_doubleWithDecimals\":2.1");
    }

    @Test
    void testEncoded() {

        GelfMessage gelfMessage = createGelfMessageWithFixedMsgId();

        String message = gelfMessage.toJson("_");

        ByteBuffer buffer = ByteBuffer.allocate(8192);
        gelfMessage.toJson(buffer, "_");

        String string = toString(buffer);

        AssertJAssertions.assertThat(string).isEqualTo(message);
    }

    @Test
    void testTcp() {

        GelfMessage gelfMessage = createGelfMessageWithFixedMsgId();

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);

        ByteBuffer oldWay = gelfMessage.toTCPBuffer();
        ByteBuffer newWay = gelfMessage.toTCPBuffer(buffer);

        AssertJAssertions.assertThat(newWay.remaining()).isEqualTo(oldWay.remaining());

        byte[] oldBytes = new byte[oldWay.remaining()];
        byte[] newBytes = new byte[newWay.remaining()];

        oldWay.get(oldBytes);
        newWay.get(newBytes);

        AssertJAssertions.assertThat(Arrays.equals(newBytes, oldBytes)).isTrue();
    }

    @Test
    void testUdp() throws Exception {

        GelfMessage gelfMessage = createGelfMessageWithFixedMsgId();

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(8192);

        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();
        ByteBuffer[] newWay = gelfMessage.toUDPBuffers(buffer, buffer2);

        AssertJAssertions.assertThat(newWay).hasSameSizeAs(oldWay);

        for (int i = 0; i < oldWay.length; i++) {

            ByteBuffer oldChunk = oldWay[i];
            ByteBuffer newChunk = newWay[i];

            byte[] oldBytes = new byte[oldChunk.remaining()];
            byte[] newBytes = new byte[newChunk.remaining()];

            oldChunk.get(oldBytes);
            newChunk.get(newBytes);

            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(newBytes));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            gzipInputStream.transferTo(baos);
            AssertJAssertions.assertThat(Arrays.equals(newBytes, oldBytes)).isTrue();
        }
    }

    @Test
    void testUdpChunked() {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            int charId = (int) (Math.random() * Character.MAX_CODE_POINT);
            builder.append(charId);
        }

        GelfMessage gelfMessage = createGelfMessageWithFixedMsgId();
        gelfMessage.setFullMessage(builder.toString());

        ByteBuffer buffer = ByteBuffer.allocateDirect(1200000);
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(60000);

        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();
        ByteBuffer[] newWay = gelfMessage.toUDPBuffers(buffer, tempBuffer);

        AssertJAssertions.assertThat(newWay.length).isEqualTo(oldWay.length);

        for (int i = 0; i < oldWay.length; i++) {

            ByteBuffer oldChunk = oldWay[i];
            ByteBuffer newChunk = newWay[i];

            byte[] oldBytes = new byte[oldChunk.remaining()];
            byte[] newBytes = new byte[newChunk.remaining()];

            oldChunk.get(oldBytes);
            newChunk.get(newBytes);

            AssertJAssertions.assertThat(Arrays.equals(newBytes, oldBytes)).isTrue();
        }
    }

    @Test
    void testGenerateMsgId() {
        GelfMessage gelfMessage = new GelfMessage() {
            @Override
            long getRandomLong() {
                return 0x804020100804A201L;
            }

            @Override
            long getCurrentTimeMillis() {
                return 0x90C06030090C1683L;
            }
        };

        AssertJAssertions.assertThat(gelfMessage.generateMsgId()).isEqualTo(0x804020100804B683L);
    }

    String toString(ByteBuffer allocate) {
        if (allocate.hasArray()) {
            return new String(allocate.array(), 0, allocate.arrayOffset() + allocate.position());
        } else {
            final byte[] b = new byte[allocate.remaining()];
            allocate.duplicate().get(b);
            return new String(b);
        }
    }

    @Test
    void testGelfMessageEmptyField() {
        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.addField("something", null);

        AssertJAssertions.assertThat(gelfMessage.toJson()).doesNotContain("something");
    }

    @Test
    @SuppressWarnings("checkstyle:methodname")
    void testGelf_v1_0() {

        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.setLevel("6");
        gelfMessage.setJavaTimestamp(123456L);

        AssertJAssertions.assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_0);
        AssertJAssertions.assertThat(gelfMessage.toJson()).contains("\"level\":\"6\"");
        AssertJAssertions.assertThat(gelfMessage.toJson()).contains("\"timestamp\":\"123.456");
    }

    @Test
    @SuppressWarnings("checkstyle:methodname")
    void testGelf_v1_1() {

        GelfMessage gelfMessage = new GelfMessage();
        gelfMessage.setLevel("6");
        gelfMessage.setJavaTimestamp(123456L);
        gelfMessage.setVersion(GelfMessage.GELF_VERSION_1_1);

        AssertJAssertions.assertThat(gelfMessage.getVersion()).isEqualTo(GelfMessage.GELF_VERSION_1_1);
        AssertJAssertions.assertThat(gelfMessage.toJson()).contains("\"level\":6");
        AssertJAssertions.assertThat(gelfMessage.toJson()).contains("\"timestamp\":123.456");
        AssertJAssertions.assertThat(gelfMessage.toJson()).contains("\"version\":\"1.1\"");

    }

    @Test
    void testGelfMessageEquality() {
        GelfMessage created = createGelfMessageWithFixedMsgId();
        GelfMessage build = buildGelfMessage();

        AssertJAssertions.assertThat(build).isEqualTo(created);
        AssertJAssertions.assertThat(created.equals(build)).isTrue();

        AssertJAssertions.assertThat(build.hashCode()).isEqualTo(created.hashCode());

        build.setFacility("other");
        AssertJAssertions.assertThat(created).isNotEqualTo(build);
    }

    @Test
    void testGelfMessageDefaults() {
        GelfMessage created = new GelfMessage();
        GelfMessage build = GelfMessageBuilder.newInstance().build();

        AssertJAssertions.assertThat(created.equals(build)).isTrue();
        AssertJAssertions.assertThat(build.hashCode()).isEqualTo(created.hashCode());
    }

    private GelfMessage createGelfMessageWithFixedMsgId() {

        GelfMessage gelfMessage = new GelfMessage() {
            @Override
            long generateMsgId() {
                return 0x8040201008048683L;
            }
        };

        gelfMessage.setFacility(FACILITY);
        gelfMessage.setVersion(VERSION);
        gelfMessage.setFullMessage(FULL_MESSAGE);
        gelfMessage.setShortMessage(SHORT_MESSAGE);
        gelfMessage.setHost(HOST);
        gelfMessage.setJavaTimestamp(TIMESTAMP);
        gelfMessage.setLevel(LEVEL);
        gelfMessage.setMaximumMessageSize(MESSAGE_SIZE);
        gelfMessage.addFields(ADDITIONAL_FIELDS);
        return gelfMessage;
    }

    private GelfMessage buildGelfMessage() {
        GelfMessageBuilder builder = GelfMessageBuilder.newInstance();

        builder.withFacility(FACILITY);
        builder.withVersion(VERSION);
        builder.withFullMessage(FULL_MESSAGE);
        builder.withShortMessage(SHORT_MESSAGE);
        builder.withHost(HOST);
        builder.withJavaTimestamp(TIMESTAMP);
        builder.withLevel(LEVEL);
        builder.withMaximumMessageSize(MESSAGE_SIZE);
        builder.withFields(ADDITIONAL_FIELDS);

        return builder.build();
    }
}
