package io.github.duoduobingbing.gelflogging4j.gelf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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


    protected static int redisLocalSentinelPort = 26379;
    private static final String redisLocalSentinelPortAsString = String.valueOf(redisLocalSentinelPort);

    private static Network network = Network.newNetwork();
    protected static String redisLocalSentinelAlias = "redis-local-sentinel";
    protected static String redisLocalMasterAlias = "redis-local-master";


    @BeforeAll
    static void beforeAll() {
        redisLocalMasterTestcontainer.withNetwork(network).withNetworkAliases(redisLocalMasterAlias);
        redisLocalMasterTestcontainer.start();


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
                        """.formatted(redisLocalSentinelPortAsString, redisLocalMasterPortAsString, redisLocalMasterPortAsString);

        final Path tempFile;
        try {
            tempFile = Files.createTempFile("sentinel", ".conf");
            Files.write(tempFile, sentinelConf.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        redisLocalSentinelTestcontainer = new GenericContainer<>("redis:8.2")
                .dependsOn(RedisNonStartingIntegrationTestBase.redisLocalMasterTestcontainer)
                .withNetwork(network).withNetworkAliases(redisLocalSentinelAlias)
                .withCommand("redis-sentinel", "/etc/sentinel.conf")
                .withEnv("SKIP_DROP_PRIVS", "1")
                .withStartupTimeout(Duration.ofSeconds(30))
                .withLogConsumer((outputFrame -> System.out.println(outputFrame.getUtf8String())))
                .withCopyFileToContainer(MountableFile.forHostPath(tempFile), "/etc/sentinel.conf");


        redisLocalSentinelTestcontainer.setExposedPorts(List.of(redisLocalSentinelPort));
        redisLocalSentinelTestcontainer.setPortBindings(List.of(redisLocalSentinelPortAsString + ":" + redisLocalSentinelPortAsString));

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
