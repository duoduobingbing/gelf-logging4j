package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.redis;

import redis.clients.jedis.UnifiedJedis;

public class RedisClientWrapper implements AutoCloseable {

    final UnifiedJedis redisClient;

    public RedisClientWrapper(UnifiedJedis redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public void close()  {
        redisClient.close();
    }

    public UnifiedJedis getRedisClient() {
        return redisClient;
    }
}
