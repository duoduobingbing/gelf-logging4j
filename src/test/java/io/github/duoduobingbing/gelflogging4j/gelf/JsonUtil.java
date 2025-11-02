package io.github.duoduobingbing.gelflogging4j.gelf;

import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;


/**
 * @author Mark Paluch
 */
public class JsonUtil {

    private static final JsonMapper jsonMapper = JsonMapper
            .builder(
                    JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build()
            )
            .build();

    private static final TypeReference<Map<String, Object>> STRING_OBJECT_MAP_TYPE_REF = new TypeReference<Map<String, Object>>() {
    };

    /**
     * Parse a JSON string to a {@link Map}
     *
     * @param jsonAsString JSON value as {@link String}.
     * @return object as {@link Map}.
     */
    public static Map<String, Object> parseToMap(String jsonAsString) {
        try {
            return jsonMapper.readValue(jsonAsString, STRING_OBJECT_MAP_TYPE_REF);
        } catch (JacksonException e) { //TODO: check if this can be ommited entirely
            throw new IllegalStateException(e);
        }
    }
}
