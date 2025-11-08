package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.net.URI;

import io.github.duoduobingbing.gelflogging4j.gelf.RedisIntegrationTestBase;
import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;
import io.github.duoduobingbing.gelflogging4j.gelf.Sockets;

/**
 * Integration tests for {@link RedisPoolHolder}.
 *
 * @author Mark Paluch
 * @author tktiki
 */
class RedisPoolHolderIntegrationTests extends RedisIntegrationTestBase {

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(Sockets.isOpen("localhost", redisMasterResolvedPort));
    }

    @Test
    void shouldConnectToJedis() {

        Pool<Jedis> pool = RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), redisMasterResolvedPort);

        Jedis resource = pool.getResource();
        AssertJAssertions.assertThat(resource.ping()).isEqualTo("PONG");
        resource.close();

        pool.destroy();
        AssertJAssertions.assertThat(pool.isClosed()).isTrue();
    }

    @Test
    void shouldConsiderRefCntBeforeClosingPool() {

        Pool<Jedis> pool1 = RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), redisMasterResolvedPort);
        Pool<Jedis> pool2 = RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), redisMasterResolvedPort);

        AssertJAssertions.assertThat(pool1.isClosed()).isFalse();
        AssertJAssertions.assertThat(pool2.isClosed()).isFalse();

        pool1.destroy();

        AssertJAssertions.assertThat(pool1.isClosed()).isFalse();
        AssertJAssertions.assertThat(pool2.isClosed()).isFalse();

        pool2.destroy();

        AssertJAssertions.assertThat(pool1.isClosed()).isTrue();
        AssertJAssertions.assertThat(pool2.isClosed()).isTrue();
    }

    @Test
    void shouldReturnActivePoolsOnly() {

        RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), redisMasterResolvedPort).destroy();
        Pool<Jedis> pool = RedisPoolHolder.getInstance().getJedisPool(URI.create("redis://localhost/1"), redisMasterResolvedPort);

        AssertJAssertions.assertThat(pool.isClosed()).isFalse();

        pool.destroy();

        AssertJAssertions.assertThat(pool.isClosed()).isTrue();
    }

}
