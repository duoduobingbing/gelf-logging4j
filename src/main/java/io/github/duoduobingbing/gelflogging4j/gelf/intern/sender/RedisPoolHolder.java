package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.Pool;

/**
 * Pool holder for {@link Pool} that keeps track of Jedis pools identified by {@link URI}.
 * <br><br>
 * This implementation synchronizes {@link #getJedisPool(URI, int)} and {@link Pool#destroy()} calls to avoid lingering
 * resources and acquisition of disposed resources. creation
 *
 * @author Mark Paluch
 * @author duoduobingbing
 */
class RedisPoolHolder {

    private final static RedisPoolHolder INSTANCE = new RedisPoolHolder();

    private final Map<String, Pool<Jedis>> standalonePools = new ConcurrentHashMap<>();

    private final Object mutex = new Object();

    public static RedisPoolHolder getInstance() {
        return INSTANCE;
    }

    public Pool<Jedis> getJedisPool(URI hostURI, int configuredPort) {

        synchronized (mutex) {

            String lowerCasedConnectionString = hostURI.toString().toLowerCase();
            final String cleanConnectionString = hostURI.getFragment() != null ? lowerCasedConnectionString.substring(0,
                    lowerCasedConnectionString.length() - hostURI.getFragment().length()) : lowerCasedConnectionString;

            if (standalonePools.containsKey(cleanConnectionString)) {


                Pool<Jedis> pool = standalonePools.get(cleanConnectionString);

                if (!(pool instanceof IWithDestroyWrapper poolWithDestroyWrapper)) {
                    throw new IllegalStateException("%s should implement %s but does not".formatted(pool.getClass().getName(), IWithDestroyWrapper.class.getName()));
                }

                poolWithDestroyWrapper.incrementRefCnt();
                return pool;
            }

            Pool<Jedis> jedisPool = JedisPoolFactory.createJedisPool(
                    hostURI,
                    configuredPort,
                    Protocol.DEFAULT_TIMEOUT,
                    (initialCloseFunction) ->
                            new MutexSynchronizedDestroyWrapper(
                                    initialCloseFunction,
                                    () -> standalonePools.remove(cleanConnectionString)
                            )
            );

            if (!(jedisPool instanceof IWithDestroyWrapper)) {
                throw new IllegalStateException("%s should implement %s but does not".formatted(jedisPool.getClass().getName(), IWithDestroyWrapper.class.getName()));
            }

            standalonePools.put(cleanConnectionString, jedisPool);

            return jedisPool;
        }
    }

    /**
     * Singleton for administration of commonly used jedis pools
     *
     * @author <a href="https://github.com/Batigoal/logstash-gelf">Batigoal</a>
     * @author Mark Paluch
     * @author duoduobingbing
     */
    private enum JedisPoolFactory {

        STANDALONE {
            @Override
            public String getScheme() {
                return RedisSenderConstants.REDIS_SCHEME;
            }

            /**
             * Create a Jedis Pool for standalone Redis Operations.
             *
             * @param hostURI hostURI
             * @param configuredPort configuredPort
             * @param timeoutMs timeoutMs
             * @param destroyWrapperSupplier {@link java.util.function.Function} that receives the original destroy method (e.g. {@code redisPool.destroy()}) call as {@link java.lang.Runnable} constructing the @{link {@link MutexSynchronizedDestroyWrapper}}
             * @return Pool of Jedis
             */
            @Override
            public Pool<Jedis> createPool(URI hostURI, int configuredPort, int timeoutMs, Function<Runnable, MutexSynchronizedDestroyWrapper> destroyWrapperSupplier) {

                String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
                int database = Protocol.DEFAULT_DATABASE;
                if (hostURI.getPath() != null && hostURI.getPath().length() > 1) {
                    database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
                }

                int port = hostURI.getPort() > 0 ? hostURI.getPort() : configuredPort;
                return new JedisPoolWithWithDestroyHook(new JedisPoolConfig(), hostURI.getHost(), port, timeoutMs, password, database, destroyWrapperSupplier);
            }

        },
        SENTINEL {

            public static final String MASTER_ID = "masterId";

            @Override
            public String getScheme() {
                return RedisSenderConstants.REDIS_SENTINEL_SCHEME;
            }

            /**
             * Create a Jedis Pool for sentinel Redis Operations.
             *
             * @param hostURI hostURI
             * @param configuredPort configuredPort
             * @param timeoutMs timeoutMs
             * @param destroyWrapperSupplier {@link java.util.function.Function} that receives the original destroy method (e.g. {@code redisPool.destroy()}) call as {@link java.lang.Runnable} constructing the @{link {@link MutexSynchronizedDestroyWrapper}}
             * @return Pool of Jedis
             */
            @Override
            public Pool<Jedis> createPool(URI hostURI, int configuredPort, int timeoutMs, Function<Runnable, MutexSynchronizedDestroyWrapper> destroyWrapperSupplier) {

                Set<String> sentinels = getSentinels(hostURI);
                String masterName = getMasterName(hostURI);

                // No logging for Jedis Sentinel at all.
                Logger.getLogger(JedisSentinelPool.class.getName()).setLevel(Level.OFF);

                String password = (hostURI.getUserInfo() != null) ? hostURI.getUserInfo().split(":", 2)[1] : null;
                int database = Protocol.DEFAULT_DATABASE;
                if (hostURI.getPath() != null && hostURI.getPath().length() > 1) {
                    database = Integer.parseInt(hostURI.getPath().split("/", 2)[1]);
                }

                return new JedisSentinelPoolWithWithDestroyHook(masterName, sentinels, new JedisPoolConfig(), timeoutMs, password, database, destroyWrapperSupplier);
            }

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

            protected Set<String> getSentinels(URI hostURI) {
                Set<String> sentinels = new HashSet<>();

                String[] sentinelHostNames = hostURI.getHost().split("\\,");
                for (String sentinelHostName : sentinelHostNames) {
                    if (sentinelHostName.contains(":")) {
                        sentinels.add(sentinelHostName);
                    } else if (hostURI.getPort() > 0) {
                        sentinels.add(sentinelHostName + ":" + hostURI.getPort());
                    }
                }
                return sentinels;
            }

        };

        public abstract String getScheme();

        abstract Pool<Jedis> createPool(URI hostURI, int configuredPort, int timeoutMs, Function<Runnable, MutexSynchronizedDestroyWrapper> destroyWrapperSupplier);

        public static Pool<Jedis> createJedisPool(URI hostURI, int configuredPort, int timeoutMs, Function<Runnable, MutexSynchronizedDestroyWrapper> destroyWrapperSupplier) {

            for (JedisPoolFactory provider : JedisPoolFactory.values()) {
                if (provider.getScheme().equals(hostURI.getScheme())) {
                    return provider.createPool(hostURI, configuredPort, timeoutMs, destroyWrapperSupplier);
                }

            }

            throw new IllegalArgumentException("Scheme " + hostURI.getScheme() + " not supported");
        }

    }

    private static class JedisPoolWithWithDestroyHook extends JedisPool implements IWithDestroyWrapper {

        private final MutexSynchronizedDestroyWrapper mutexSynchronizedDestroyWrapper;

        public JedisPoolWithWithDestroyHook(
                GenericObjectPoolConfig<Jedis> poolConfig,
                String host,
                int port,
                int timeout,
                String password,
                int database,
                Function<Runnable, MutexSynchronizedDestroyWrapper> destroyWrapperSupplier
        ) {
            super(poolConfig, host, port, timeout, password, database);
            this.mutexSynchronizedDestroyWrapper = destroyWrapperSupplier.apply(super::destroy);
        }

        @Override
        public void destroy() {
            mutexSynchronizedDestroyWrapper.destroy();
        }

        @Override
        public void incrementRefCnt() {
            mutexSynchronizedDestroyWrapper.incrementRefCnt();
        }

    }


    private static class JedisSentinelPoolWithWithDestroyHook extends JedisSentinelPool implements IWithDestroyWrapper {

        public JedisSentinelPoolWithWithDestroyHook(
                String masterName,
                Set<String> sentinels,
                GenericObjectPoolConfig<Jedis> poolConfig,
                int timeout,
                String password,
                int database,
                Function<Runnable, MutexSynchronizedDestroyWrapper> destroyWrapperSupplier
        ) {
            super(masterName, sentinels, poolConfig, timeout, timeout, password, database);
            this.mutexSynchronizedDestroyWrapper = destroyWrapperSupplier.apply(super::destroy);
        }

        private final MutexSynchronizedDestroyWrapper mutexSynchronizedDestroyWrapper;

        @Override
        public void destroy() {
            mutexSynchronizedDestroyWrapper.destroy();
        }

        @Override
        public void incrementRefCnt() {
            mutexSynchronizedDestroyWrapper.incrementRefCnt();
        }

    }

    interface IWithDestroyWrapper {

        public void incrementRefCnt();
    }

    class MutexSynchronizedDestroyWrapper {

        private final AtomicLong refCnt = new AtomicLong(1);

        /**
         * Runnable that is invoked before the real .destroy() call is made
         */
        private final Runnable onDestroy;

        /**
         * A runnable wrapping the real destroy call e.g. redisPool.destroy()
         */
        private final Runnable realDestroyMethodRunnable;

        MutexSynchronizedDestroyWrapper(Runnable realDestroyMethodRunnable, Runnable onDestroy) {
            this.realDestroyMethodRunnable = Objects.requireNonNull(realDestroyMethodRunnable);
            this.onDestroy = Objects.requireNonNull(onDestroy);
        }

        public void incrementRefCnt() {
            refCnt.incrementAndGet();
        }

        public void destroy() {
            synchronized (mutex) {

                long val = refCnt.decrementAndGet();

                if (val != 0) {
                    return;
                }

                onDestroy.run();

                realDestroyMethodRunnable.run();

            }
        }
    }


}
