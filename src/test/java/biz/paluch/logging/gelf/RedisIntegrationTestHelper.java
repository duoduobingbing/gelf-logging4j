package biz.paluch.logging.gelf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class RedisIntegrationTestHelper {

    // Redis Container (Redis 2.8 wie im Makefile)
    public static GenericContainer<?> redisContainer;

    public static Jedis jedis;

    @BeforeAll
    static void beforeAll() {

        List<String> portBindings = new ArrayList<>();
        portBindings.add("6479:6479");
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:2.8"))
                .withExposedPorts(6479)

                .withCommand("redis-server", "--port", "6479",
                        "--bind", "0.0.0.0",
                        "--save", "",
                        "--appendonly", "no",
                        "--daemonize", "no");

        redisContainer.setPortBindings(portBindings);
        redisContainer.start();

        String redisHost = redisContainer.getHost();
        Integer redisPort = redisContainer.getMappedPort(6479);


        jedis = new Jedis(redisHost, redisPort);
        jedis.flushDB();
        jedis.flushAll();
    }

    @AfterAll
    static void afterAll() {
        if (jedis != null) {
            jedis.close();
        }
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }


}
