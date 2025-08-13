package biz.paluch.logging.gelf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RedisSentinelIntegrationTestHelper extends RedisIntegrationTestHelper{

    public static GenericContainer<?> redisLocalSentinel;


    public static int redisLocalSentinelPort = 26379;
    private static final String redisLocalSentinelPortAsString = String.valueOf(redisLocalSentinelPort);

    private static Network network = Network.newNetwork();
    public static String redisLocalSentinelAlias = "redis-local-sentinel";
    public static String redisLocalMasterAlias = "redis-local-master";


    @BeforeAll
    static void beforeAll() {
        RedisIntegrationTestHelper.createRedisMasterTestcontainer();
        redisLocalMasterTestcontainer.withNetwork(network).withNetworkAliases(redisLocalMasterAlias);
        RedisIntegrationTestHelper.startRedisMasterTestcontainer();

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


        redisLocalSentinel = new GenericContainer<>("redis:2.8")
                .withExposedPorts(redisLocalSentinelPort)
                .withNetwork(network).withNetworkAliases(redisLocalSentinelAlias)
                .withCommand("redis-sentinel", "/etc/sentinel.conf")
                .withStartupTimeout(Duration.ofSeconds(30))
                .withCopyFileToContainer(MountableFile.forHostPath(tempFile), "/etc/sentinel.conf");


        redisLocalSentinel.setPortBindings(List.of(redisLocalSentinelPortAsString + ":" + redisLocalSentinelPortAsString));
        redisLocalSentinel.start();
    }

    @AfterAll
    static void stopRedisSentinel() {
        if (redisLocalSentinel != null) {
            redisLocalSentinel.stop();
        }
        if (network != null){
            network.close();
        }
    }
}
