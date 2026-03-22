package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.redis;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;

/**
 * Pool holder for {@link UnifiedJedis} that keeps track of clients identified by {@link URI}.
 * <br><br>
 * This implementation synchronizes creation and {@link UnifiedJedis#close()} calls to avoid lingering
 * resources and acquisition of disposed resources. creation
 *
 * @author Mark Paluch
 * @author duoduobingbing
 */
class RedisClientHolder {

    private RedisClientHolder() {
    }

    private final static RedisClientHolder INSTANCE = new RedisClientHolder();

    private final Map<String, SharedRedisClientWrapper> standaloneClients = new ConcurrentHashMap<>();

    private final Object mutex = new Object();

    public static RedisClientHolder getInstance() {
        return INSTANCE;
    }

    RedisClientWrapper getRedisClient(URI hostURI, int configuredPort) {

        synchronized (mutex) {

            final String lowerCasedConnectionString = hostURI.toString().toLowerCase();

            final String cleanConnectionString;

            if (hostURI.getFragment() != null) {
                cleanConnectionString = lowerCasedConnectionString.substring(0, lowerCasedConnectionString.length() - hostURI.getFragment().length());
            } else {
                cleanConnectionString = lowerCasedConnectionString;
            }

            if (standaloneClients.containsKey(cleanConnectionString)) {
                SharedRedisClientWrapper client = standaloneClients.get(cleanConnectionString);
                client.increaseCountOfReferences();
                return client;
            }

            String scheme = hostURI.getScheme();

            RedisClientFactory<? extends UnifiedJedis> redisClientFactory = switch (scheme) {
                case RedisSenderConstants.REDIS_SCHEME -> new StandaloneRedisClientFactory();
                case RedisSenderConstants.REDIS_SENTINEL_SCHEME -> new SentinelRedisClientFactory();
                case null, default -> throw new IllegalArgumentException("Scheme %s not supported".formatted(scheme));
            };

            UnifiedJedis rawClient = redisClientFactory.createClient(hostURI, configuredPort, Protocol.DEFAULT_TIMEOUT);

            SharedRedisClientWrapper redisClientWrapper = new SharedRedisClientWrapper(
                    rawClient,
                    () -> standaloneClients.remove(cleanConnectionString)
            );

            standaloneClients.put(cleanConnectionString, redisClientWrapper);

            return redisClientWrapper;
        }
    }

    protected class SharedRedisClientWrapper extends RedisClientWrapper {

        private final Runnable onClose;

        private final AtomicLong refCnt = new AtomicLong(1);

        protected SharedRedisClientWrapper(UnifiedJedis redisClient, Runnable onClose) {
            super(redisClient);
            this.onClose = onClose;
        }

        protected void increaseCountOfReferences() {
            refCnt.incrementAndGet();
        }

        @Override
        public void close() {
            synchronized (mutex) {
                long val = refCnt.decrementAndGet();

                if (val != 0) {
                    return;
                }

                onClose.run(); //only remove from list of hold open references, when all are closed i.e. val = 0

                super.close();

            }

        }

    }


}
