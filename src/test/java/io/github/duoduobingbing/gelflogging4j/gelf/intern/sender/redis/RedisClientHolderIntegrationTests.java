package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.redis;

import java.net.URI;

import io.github.duoduobingbing.gelflogging4j.gelf.RedisIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.UnifiedJedis;
import io.github.duoduobingbing.gelflogging4j.gelf.Sockets;

/**
 * Integration tests for {@link RedisClientHolder}.
 *
 * @author Mark Paluch
 * @author tktiki
 */
class RedisClientHolderIntegrationTests extends RedisIntegrationTestBase {

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(Sockets.isOpen("localhost", redisMasterResolvedPort));
    }

    @Test
    void shouldConnectToJedis() {

        RedisClientWrapper clientWrapper = RedisClientHolder.getInstance().getRedisClient(URI.create("redis://localhost/1"), redisMasterResolvedPort);
        UnifiedJedis redisClient = clientWrapper.getRedisClient();

        AssertJAssertions.assertThat(redisClient.ping()).isEqualTo("PONG");
        clientWrapper.close();

        redisClient.close();

        AssertJAssertions.assertThatThrownBy(redisClient::ping).hasRootCauseMessage("Pool not open");
    }

    @Test
    void shouldConsiderRefCntBeforeClosingPool() {
//
        RedisClientWrapper clientWrapper1 = RedisClientHolder.getInstance().getRedisClient(URI.create("redis://localhost/1"), redisMasterResolvedPort);
        RedisClientWrapper clientWrapper2 = RedisClientHolder.getInstance().getRedisClient(URI.create("redis://localhost/1"), redisMasterResolvedPort);

        AssertJAssertions.assertThatCode(() -> clientWrapper1.getRedisClient().ping()).doesNotThrowAnyException();
        AssertJAssertions.assertThatCode(() -> clientWrapper2.getRedisClient().ping()).doesNotThrowAnyException();

        clientWrapper1.close();

        AssertJAssertions.assertThatCode(() -> clientWrapper1.getRedisClient().ping()).doesNotThrowAnyException();
        AssertJAssertions.assertThatCode(() -> clientWrapper2.getRedisClient().ping()).doesNotThrowAnyException();

        clientWrapper2.close();

        AssertJAssertions.assertThatThrownBy(() -> clientWrapper1.getRedisClient().ping()).hasRootCauseMessage("Pool not open");
        AssertJAssertions.assertThatThrownBy(() -> clientWrapper2.getRedisClient().ping()).hasRootCauseMessage("Pool not open");
    }

    @Test
    void shouldReturnActivePoolsOnly() {

        RedisClientWrapper clientWrapper1 = RedisClientHolder.getInstance().getRedisClient(URI.create("redis://localhost/1"), redisMasterResolvedPort);
        clientWrapper1.close();

        RedisClientWrapper clientWrapper2 = RedisClientHolder.getInstance().getRedisClient(URI.create("redis://localhost/1"), redisMasterResolvedPort);
        AssertJAssertions.assertThatCode(() -> clientWrapper2.getRedisClient().ping()).doesNotThrowAnyException();

        clientWrapper2.close();
        AssertJAssertions.assertThatThrownBy(() -> clientWrapper2.getRedisClient().ping()).hasRootCauseMessage("Pool not open");
    }

}
