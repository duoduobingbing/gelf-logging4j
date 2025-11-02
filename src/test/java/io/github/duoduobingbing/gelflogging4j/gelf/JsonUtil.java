package io.github.duoduobingbing.gelflogging4j.gelf;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * @author Mark Paluch
 */
public class JsonUtil {

    private static final JsonMapper jsonMapper = JsonMapper
            .builder(
                    JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build()
            )
            .build();

    /**
     * Parse a JSON string to a {@link Map}
     *
     * @param jsonAsString JSON value as {@link String}.
     * @return object as {@link Map}.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String jsonAsString) {
        try {
            return jsonMapper.readValue(jsonAsString, Map.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
