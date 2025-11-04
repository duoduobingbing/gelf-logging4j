package io.github.duoduobingbing.gelflogging4j.gelf.test.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DockerFileUtil {

    private DockerFileUtil() {
    }

    /**
     * Parses the first found image from a Dockerfile. Throws an Exception if nothing was found inside the file or there was no file.
     * @param dockerFilePath classpath file path to the docker file
     * @return image
     */
    public static String parseDockerImageFromClassPathFile(String dockerFilePath) {
        try (var is = DockerFileUtil.class.getClassLoader().getResourceAsStream(dockerFilePath)) {
            String fileContent = new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
            Matcher m = Pattern.compile("^\\s*FROM\\s*(.+)\\s*", Pattern.MULTILINE).matcher(fileContent);
            if(!m.find()){
                throw new IllegalStateException("Docker image not found. There seems to be no FROM image section in your Dockerfile");
            }

            return m.group(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
