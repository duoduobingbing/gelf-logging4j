package biz.paluch.logging.gelf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tktiki
 * @author duoduobingbing
 */
@Testcontainers
public class RedisIntegrationTestBase {

    protected static GenericContainer<?> redisLocalMasterTestcontainer;

    protected static Jedis jedisMaster;

    protected static int redisLocalMasterPort = 6479;
    protected static final String redisLocalMasterPortAsString = String.valueOf(redisLocalMasterPort);

    @BeforeAll
    static void beforeAll() {
        createRedisMasterTestcontainer();
        startRedisMasterTestcontainer();
    }

    protected static void startRedisMasterTestcontainer() {
        redisLocalMasterTestcontainer.start();
    }

    protected static void createRedisMasterTestcontainer() {
        redisLocalMasterTestcontainer = new GenericContainer<>(DockerImageName.parse("redis:8.2"));

        final List<String> portBindings = new ArrayList<>();
        portBindings.add(redisLocalMasterPortAsString + ":" + redisLocalMasterPortAsString);
        redisLocalMasterTestcontainer
                .withExposedPorts(redisLocalMasterPort)
                .withLogConsumer((outputFrame -> System.out.println(outputFrame.getUtf8String())))
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
        jedisMaster = createAndGetJedis();
    }

    static Jedis createAndGetJedis() {
        if (!redisLocalMasterTestcontainer.isRunning()) {
            throw new IllegalStateException("Redis master is not running");
        }
        final String redisHost = redisLocalMasterTestcontainer.getHost();
        final int redisPort = redisLocalMasterTestcontainer.getMappedPort(redisLocalMasterPort);


        final Jedis jedisM = new Jedis(redisHost, redisPort);
        jedisM.flushDB();
        jedisM.flushAll();

        return jedisM;
    }

    @AfterAll
    static void afterAll() {
        if (jedisMaster != null) {
            jedisMaster.close();
        }
        if (redisLocalMasterTestcontainer != null) {
            redisLocalMasterTestcontainer.stop();
        }
    }


}
