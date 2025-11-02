package io.github.duoduobingbing.gelflogging4j.gelf.test.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class PropertiesHelper {

    private PropertiesHelper() {
    }

    public static Map<String, String> propertiesToMap(Properties properties) {
        HashMap<String, String> map = new HashMap<>();

        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            map.put(
                    Objects.toString(Objects.requireNonNull(entry.getKey())),
                    Objects.toString(entry.getValue(), null)
            );
        }

        return map;
    }

}
