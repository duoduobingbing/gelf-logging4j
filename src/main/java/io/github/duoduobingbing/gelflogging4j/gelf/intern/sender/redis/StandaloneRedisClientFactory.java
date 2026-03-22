package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.redis;

import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.RedisClient;

import java.net.URI;

class StandaloneRedisClientFactory implements RedisClientFactory<RedisClient> {

    @Override
    public RedisClient createClient(URI hostURI, int configuredPort, int timeoutMillis) {

        int port = hostURI.getPort() > 0 ? hostURI.getPort() : configuredPort;

        final JedisClientConfig jedisClientConfig = RedisClientFactory.createJedisClientConfig(hostURI, timeoutMillis);

        RedisClient client = RedisClient
                .builder()
                .clientConfig(jedisClientConfig)
                .hostAndPort(hostURI.getHost(), port)
                .poolConfig(new ConnectionPoolConfig())
                .build();

        return client;
    }
}
