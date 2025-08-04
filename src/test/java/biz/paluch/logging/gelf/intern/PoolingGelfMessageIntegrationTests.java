package biz.paluch.logging.gelf.intern;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.jboss.as.protocol.StreamUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import biz.paluch.logging.StackTraceFilter;
import org.junit.jupiter.api.function.Executable;

class PoolingGelfMessageIntegrationTests {

    private static final String FACILITY = "facility";
    private static final String VERSION = "2.0";
    private static final String FULL_MESSAGE = "full";
    private static final String SHORT_MESSAGE = "short";
    private static final String HOST = "host";
    private static final String LEVEL = "5";
    private static final long TIMESTAMP = 42;
    private static final int MESSAGE_SIZE = 5344;

    private static final Map<String, String> ADDITIONAL_FIELDS = new HashMap<String, String>() {
        {
            put("a", "b");
            put("doubleNoDecimals", "2.0");
            put("doubleWithDecimals", "2.1");
            put("int", "2");
            put("exception1", StackTraceFilter.getFilteredStackTrace(new IOException(new Exception(new Exception()))));
            put("exception2",
                    StackTraceFilter.getFilteredStackTrace(new IllegalStateException(new Exception(new Exception()))));
            put("exception3", StackTraceFilter
                    .getFilteredStackTrace(new IllegalArgumentException(new Exception(new IllegalArgumentException()))));
        }
    };

    static String convertGzipBytesToString(byte[] bytes) throws IOException {
        String result;
        try (
                GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ) {
            StreamUtils.copyStream(gzipInputStream, baos);
            result = baos.toString(StandardCharsets.UTF_8);
        }
        return result;
    }

    @Test
    void testUdp() throws Exception {

        GelfMessage gelfMessage = createGelfMessage();
        PoolingGelfMessage poolingGelfMessage = createPooledGelfMessage();


        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();

        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(8192);

        ByteBuffer[] newWay = poolingGelfMessage.toUDPBuffers(buffer, tempBuffer);

        assertThat(newWay.length).isEqualTo(oldWay.length);

        for (int i = 0; i < oldWay.length; i++) {

            ByteBuffer oldChunk = oldWay[i];
            ByteBuffer newChunk = newWay[i];

            byte[] oldBytes = new byte[oldChunk.remaining()];
            byte[] newBytes = new byte[newChunk.remaining()];

            oldChunk.get(oldBytes);
            newChunk.get(newBytes);

            String s1 = convertGzipBytesToString(oldBytes);
            String s2 = convertGzipBytesToString(newBytes);

            assertThat(s2).isEqualTo(s1);

            //TODO: for whatever reasons original gzip bytes are different
//            assertThat(newBytes).containsExactly(oldBytes);
//            assertThat(Arrays.equals(newBytes, oldBytes)).isTrue();
        }
    }

    static byte[] unchunk(byte[] chunk) throws IOException {
            //GELF_CHUNKED_ID -> 2 Bytes
            //Message ID -> 8 Bytes
            //datagram sequence number -> 1 Byte
            //total number of datagrams -> 1 Byte
            byte[] segment = new byte[chunk.length - 12];
            System.arraycopy(chunk, 12, segment, 0, segment.length);
            return segment;
    }

    @Test
    void testUdpChunked() throws Exception {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            int charId = (int) (Math.random() * Character.MAX_CODE_POINT);
            builder.append(charId);
        }

        GelfMessage gelfMessage = createGelfMessage();
        PoolingGelfMessage poolingGelfMessage = createPooledGelfMessage();

        gelfMessage.setFullMessage(builder.toString());
        poolingGelfMessage.setFullMessage(builder.toString());

        ByteBuffer buffer = ByteBuffer.allocateDirect(1200000);
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(60000);

        ByteBuffer[] oldWay = gelfMessage.toUDPBuffers();
        ByteBuffer[] newWay = poolingGelfMessage.toUDPBuffers(buffer, tempBuffer);

        assertThat(newWay.length).isEqualTo(oldWay.length);

        ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();

        for (int i = 0; i < oldWay.length; i++) {

            ByteBuffer oldChunk = oldWay[i];
            ByteBuffer newChunk = newWay[i];

            byte[] oldBytes = new byte[oldChunk.remaining()];
            byte[] newBytes = new byte[newChunk.remaining()];

            oldChunk.get(oldBytes);
            newChunk.get(newBytes);

            byteArrayOutputStream1.write(unchunk(oldBytes));
            byteArrayOutputStream2.write(unchunk(newBytes));


        }

        byte[] barr1 = byteArrayOutputStream1.toByteArray();
        String s1 = convertGzipBytesToString(barr1);

        byte[] barr2 = byteArrayOutputStream2.toByteArray();
        String s2 = convertGzipBytesToString(barr2);


        assertThat(s1).isEqualTo(s2);
    }

    private GelfMessage createGelfMessage() {

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

    private PoolingGelfMessage createPooledGelfMessage() {

        PoolingGelfMessage gelfMessage = new PoolingGelfMessage(PoolHolder.threadLocal()) {
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
}
