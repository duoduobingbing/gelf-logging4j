package io.github.duoduobingbing.gelflogging4j.gelf.test.helper;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class PropertiesHelper {

    private PropertiesHelper() {
    }

    /**
     * Properties is a <code>Map&lt;Object, Object&gt;</code>, so use this to convert all keys and values to strings
     * {@code null} as key is not allowed.
     *
     * @param properties to convert to a <code>Map&lt;String, String&gt;</code>
     * @return String-Map
     */
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

    /**
     * Replaces the given {@code portInProperties} inside the entire resource with the resolved port of the {@code exposedPort} obtained from the {@code container}
     * and returns the result as a new {@link InputStream}
     * @param resourcePath classpath to the properties resource
     * @param container container to resolve {@code exposedPort} against
     * @param exposedPort local port inside the container that is exposed
     * @param portInProperties port to be replaced in the given resource
     * @return new {@link InputStream} with the replaced properties resource
     * @throws IOException resource could not be loaded
     */
    public static InputStream replacePortInResource(String resourcePath, GenericContainer<?> container, int exposedPort, int portInProperties) throws IOException {
        int resolvedPort = container.getMappedPort(exposedPort);

        InputStream resolvedPropertiesStream;

        //The properties use port the hard-coded, however the docker container resolves to a different port every time, so we just replace it naively
        //Of course this leads to trouble if the same port number appears in a different context in the same file and is not meant to be replaced.
        try (InputStream propertiesStream = PropertiesHelper.class.getResourceAsStream(resourcePath)) {
            String s = new String(Objects.requireNonNull(propertiesStream).readAllBytes(), StandardCharsets.UTF_8);
            LoggerFactory.getLogger(PropertiesHelper.class).info("Replacing port {} with {} in config", portInProperties, resolvedPort);
            String replaceProperties = s.replaceAll(String.valueOf(portInProperties), String.valueOf(resolvedPort)); //naively replace the number everywhere in the file
            resolvedPropertiesStream = new ByteArrayInputStream(replaceProperties.getBytes(StandardCharsets.UTF_8));
        }

        return resolvedPropertiesStream;
    }

}
