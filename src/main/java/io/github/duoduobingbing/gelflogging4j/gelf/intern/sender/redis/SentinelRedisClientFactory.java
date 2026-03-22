package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.redis;

import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.RedisSentinelClient;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

class SentinelRedisClientFactory implements RedisClientFactory<RedisSentinelClient> {

    @Override
    public RedisSentinelClient createClient(URI hostURI, int configuredPort, int timeoutMillis) {
        Set<HostAndPort> sentinels = getSentinels(hostURI);
        String masterName = getMasterName(hostURI);

        final JedisClientConfig jedisClientConfig = RedisClientFactory.createJedisClientConfig(hostURI, timeoutMillis);

        RedisSentinelClient redisSentinelClient = RedisSentinelClient
                .builder()
                .clientConfig(jedisClientConfig)
                .poolConfig(new ConnectionPoolConfig())
                .masterName(masterName)
                .sentinels(sentinels)
                .build();

        return redisSentinelClient;
    }


    public static final String MASTER_ID = "masterId";

    protected String getMasterName(URI hostURI) {
        String masterName = "master";

        if (hostURI.getQuery() != null) {
            String[] keyValues = hostURI.getQuery().split("\\&");
            for (String keyValue : keyValues) {
                String[] parts = keyValue.split("\\=");
                if (parts.length != 2) {
                    continue;
                }

                if (parts[0].equals(MASTER_ID)) {
                    masterName = parts[1].trim();
                }
            }
        }
        return masterName;
    }

    protected Set<HostAndPort> getSentinels(URI hostURI) {
        Set<HostAndPort> sentinels = new HashSet<>();

        String[] sentinelHostNames = hostURI.getHost().split("\\,");

        for (String sentinelHostName : sentinelHostNames) {
            if (sentinelHostName.contains(":")) {
                sentinels.add(HostAndPort.from(sentinelHostName));
            } else if (hostURI.getPort() > 0) {
                sentinels.add(HostAndPort.from(sentinelHostName + ":" + hostURI.getPort()));
            }
        }

        return sentinels;
    }
}
