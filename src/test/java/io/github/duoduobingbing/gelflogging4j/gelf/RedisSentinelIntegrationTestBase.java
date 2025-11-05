package io.github.duoduobingbing.gelflogging4j.gelf;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.DockerFileUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * @author tktiki
 * @author duoduobingbing
 */
public class RedisSentinelIntegrationTestBase extends RedisNonStartingIntegrationTestBase {

    protected static GenericContainer<?> redisLocalSentinelTestcontainer;

    private static final Logger logger = LoggerFactory.getLogger(RedisSentinelIntegrationTestBase.class);

    protected static int redisLocalSentinelPort = 26379;
    private static final String redisLocalSentinelPortAsString = String.valueOf(redisLocalSentinelPort);

    private static Network network = Network.newNetwork();
    protected static String redisLocalSentinelAlias = "redis-local-sentinel";
    protected static String redisLocalMasterAlias = "redis-local-master";


    @BeforeAll
    static void beforeAll() {
        GenericContainer<?> masterTestcontainer = redisLocalMasterTestcontainer
                .withNetwork(network)
                .withNetworkAliases(redisLocalMasterAlias)
                .withExposedPorts(redisLocalMasterPort);

        masterTestcontainer.start();
        redisMasterResolvedPort = masterTestcontainer.getMappedPort(redisLocalMasterPort);

        final String sentinelConf =
                """
                port %s
                bind 0.0.0.0
                sentinel monitor mymaster 127.0.0.1 %s 1
                sentinel announce-ip 127.0.0.1
                sentinel announce-port %s
                sentinel down-after-milliseconds mymaster 2000
                sentinel failover-timeout mymaster 120000
                sentinel parallel-syncs mymaster 1
                """.formatted(redisLocalSentinelPortAsString, redisMasterResolvedPort, redisLocalMasterPortAsString);

        System.out.println(sentinelConf);

        final Path tempFile;
        try {
            tempFile = Files.createTempFile("sentinel", ".conf");
            Files.writeString(tempFile, sentinelConf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String redisDockerImage = DockerFileUtil.parseDockerImageFromClassPathFile("docker/Redis.Dockerfile");
        logger.info("Using redis docker image: {}", redisDockerImage);

        redisLocalSentinelTestcontainer = new GenericContainer<>(redisDockerImage)
                .dependsOn(RedisNonStartingIntegrationTestBase.redisLocalMasterTestcontainer)
                .withNetwork(network)
                .withNetworkAliases(redisLocalSentinelAlias)
                .withCommand("redis-sentinel", "/etc/sentinel.conf")
//                .withCommand("sh", "-c", "chown redis:redis /etc/sentinel.conf && redis-sentinel /etc/sentinel.conf") //can be used instead of SKIP_DROP_PRIVS=1
                .withEnv("SKIP_DROP_PRIVS", "1")
                .withStartupTimeout(Duration.ofSeconds(30))
                .withLogConsumer((outputFrame -> logger.info(outputFrame.getUtf8StringWithoutLineEnding())))
                .withCopyFileToContainer(MountableFile.forHostPath(tempFile), "/etc/sentinel.conf");


        redisLocalSentinelTestcontainer.setExposedPorts(List.of(redisLocalSentinelPort));
        //Enable line below for fixed port - use for test debugging only!
//        redisLocalSentinelTestcontainer.setPortBindings(List.of(redisLocalSentinelPortAsString + ":" + redisLocalSentinelPortAsString));

        redisLocalSentinelTestcontainer.start();

    }

    @AfterAll
    static void stopRedisSentinel() {
        if (redisLocalSentinelTestcontainer != null) {
            redisLocalSentinelTestcontainer.stop();
        }
        if (network != null) {
            network.close();
        }
    }
}
