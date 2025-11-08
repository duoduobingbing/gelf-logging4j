package io.github.duoduobingbing.gelflogging4j.gelf.intern;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.github.duoduobingbing.gelflogging4j.gelf.JsonUtil;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Mark Paluch
 */
class JsonWriterUnitTests {

    private String content;

    @BeforeEach
    void before() throws Exception {

        byte[] bytes;
        try (InputStream stream = getClass().getResourceAsStream("/utf8.txt")) {
            bytes = stream.readAllBytes();
        }

        content = new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    void testUtf8Encoding() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("key", content);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonWriter.toJSONString(OutputAccessor.from(buffer), map);

        JsonMapper jsonMapper = JsonUtil.createJsonMapper();
        Map<?, ?> parsedByJackson = jsonMapper.readValue(buffer.toByteArray(), Map.class);
        AssertJAssertions.assertThat(parsedByJackson).isEqualTo(map);
    }

    @Test
    void testUtf8EncodingWithJacksonEncoding() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("key", content);

        JsonMapper jsonMapper = JsonUtil.createJsonMapper();
        Map<?, ?> parsedByJackson = jsonMapper.readValue(jsonMapper.writeValueAsBytes(map), Map.class);

        AssertJAssertions.assertThat(parsedByJackson).isEqualTo(map);
    }

    static final TypeReference<Map<String, Object>> STRING_OBJECT_MAP_REF = new TypeReference<Map<String, Object>>() {
    };

    @Test
    void testTypeEncoding() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("key", "string");
        map.put("double", Double.MAX_VALUE);
        map.put("doublePosInfinite", Double.POSITIVE_INFINITY);
        map.put("doubleNegInfinite", Double.NEGATIVE_INFINITY);
        map.put("doubleNaN", Double.NaN);
        map.put("int", 1);

        Map<String, Object> expected = new HashMap<>();
        expected.put("key", "string");
        expected.put("double", Double.MAX_VALUE);
        expected.put("doublePosInfinite", "Infinite");
        expected.put("doubleNegInfinite", "-Infinite");
        expected.put("doubleNaN", "NaN");
        expected.put("int", 1);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonWriter.toJSONString(OutputAccessor.from(buffer), map);

        JsonMapper jsonMapper = JsonUtil.createJsonMapper();
        Map<String, Object> parsedByJackson = jsonMapper.readValue(buffer.toByteArray(), STRING_OBJECT_MAP_REF);
        AssertJAssertions.assertThat(parsedByJackson).isEqualTo(expected);
    }
}
