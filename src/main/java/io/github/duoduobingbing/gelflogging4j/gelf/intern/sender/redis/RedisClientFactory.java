package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.redis;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;

import java.net.URI;

interface RedisClientFactory<E extends UnifiedJedis> {

    public E createClient(URI hostURI, int configuredPort, int timeoutMillis);

    static JedisClientConfig createJedisClientConfig(URI hostURI, int timeoutMillis) {
        String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
        int database = Protocol.DEFAULT_DATABASE;
        if (hostURI.getPath() != null && hostURI.getPath().length() > 1) {
            database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
        }

        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig
                .builder()
                .password(password)
                .database(database)
                .timeoutMillis(timeoutMillis)
                .build();

        return jedisClientConfig;
    }

}
