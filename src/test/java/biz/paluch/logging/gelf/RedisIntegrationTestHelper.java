package biz.paluch.logging.gelf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RedisIntegrationTestHelper {

    public static GenericContainer<?> redisLocalMasterTestcontainer;

    public static Jedis jedis;

    public static int redisLocalMasterPort = 6479;
    public static final String redisLocalMasterPortAsString = String.valueOf(redisLocalMasterPort);

    @BeforeAll
    static void beforeAll() {
        createRedisMasterTestcontainer();
        startRedisMasterTestcontainer();

    }

    static void startRedisMasterTestcontainer() {
        redisLocalMasterTestcontainer.start();
    }

    static void createRedisMasterTestcontainer() {
        redisLocalMasterTestcontainer = new GenericContainer<>(DockerImageName.parse("redis:2.8"));

        final List<String> portBindings = new ArrayList<>();
        portBindings.add(redisLocalMasterPortAsString + ":" + redisLocalMasterPortAsString);
        redisLocalMasterTestcontainer
                .withExposedPorts(redisLocalMasterPort)

                .withCommand("redis-server", "--port", redisLocalMasterPortAsString,
                        "--bind", "0.0.0.0",
                        "--save", "",
                        "--appendonly", "no",
                        "--daemonize", "no")
                .withStartupTimeout(Duration.ofSeconds(30));

        redisLocalMasterTestcontainer.setPortBindings(portBindings);

    }

    @BeforeEach
    void beforeEachTest() {
        jedis = createAndGetJedis();
    }

    static Jedis createAndGetJedis() {
        if (!redisLocalMasterTestcontainer.isRunning()) {
            throw new IllegalStateException("Redis master is not running");
        }
        final String redisHost = redisLocalMasterTestcontainer.getHost();
        final int redisPort = redisLocalMasterTestcontainer.getMappedPort(redisLocalMasterPort);


        jedis = new Jedis(redisHost, redisPort);
        jedis.flushDB();
        jedis.flushAll();

        return jedis;
    }

    @AfterAll
    static void afterAll() {
        if (jedis != null) {
            jedis.close();
        }
        if (redisLocalMasterTestcontainer != null) {
            redisLocalMasterTestcontainer.stop();
        }
    }


}
