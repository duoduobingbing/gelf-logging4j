package io.github.duoduobingbing.gelflogging4j.gelf;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.DockerFileUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

import java.time.Duration;

/**
 * @author tktiki
 * @author duoduobingbing
 */
@Testcontainers
public class RedisNonStartingIntegrationTestBase {

    protected static GenericContainer<?> redisLocalMasterTestcontainer;

    protected static Jedis jedisMaster;

    protected static int redisMasterResolvedPort;

    protected static final int redisLocalMasterPort = 6479;
    protected static final String redisLocalMasterPortAsString = String.valueOf(redisLocalMasterPort);

    protected static final Logger logger = LoggerFactory.getLogger(RedisNonStartingIntegrationTestBase.class);

    @BeforeAll
    static void beforeAll() {
        createRedisMasterTestcontainer();
    }


    protected static void startRedisMasterTestcontainer() {
        redisLocalMasterTestcontainer.start();
        redisMasterResolvedPort = redisLocalMasterTestcontainer.getMappedPort(redisLocalMasterPort);
    }

    protected static void createRedisMasterTestcontainer() {
        String redisDockerImage = DockerFileUtil.parseDockerImageFromClassPathFile("docker/Redis.Dockerfile");
        logger.info("Using redis docker image: {}", redisDockerImage);
        redisLocalMasterTestcontainer = new GenericContainer<>(DockerImageName.parse(redisDockerImage));



        redisLocalMasterTestcontainer
                .withExposedPorts(redisLocalMasterPort)
                .withLogConsumer((outputFrame -> logger.info(outputFrame.getUtf8StringWithoutLineEnding())))
                .withCommand("redis-server", "--port", redisLocalMasterPortAsString,
                        "--bind", "0.0.0.0",
                        "--save", "",
                        "--appendonly", "no",
                        "--daemonize", "no")
                .withStartupTimeout(Duration.ofSeconds(30));

        //Use below for fixed port for debugging the tests
//        final List<String> portBindings = new ArrayList<>();
//        portBindings.add(redisLocalMasterPortAsString + ":" + redisLocalMasterPortAsString);
//        redisLocalMasterTestcontainer.setPortBindings(portBindings);

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
