package io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.redis;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.ErrorReporter;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfSender;

/**
 * @author https://github.com/strima/logstash-gelf.git
 * @author Mark Paluch
 * @author duoduobingbing
 * @since 1.5
 */
public class GelfREDISSender implements GelfSender {

    private final RedisClientWrapper redisClientWrapper;
    private final ErrorReporter errorReporter;
    private final String redisKey;
    final Set<Thread> callers = Collections.newSetFromMap(new WeakHashMap<Thread, Boolean>());

    public GelfREDISSender(RedisClientWrapper redisClientWrapper, String redisKey, ErrorReporter errorReporter) {
        this.redisClientWrapper = redisClientWrapper;
        this.errorReporter = errorReporter;
        this.redisKey = redisKey;
    }

    public boolean sendMessage(GelfMessage message) {

        // prevent recursive self calls caused by the Redis driver since it
        if (!callers.add(Thread.currentThread())) {
            return false;
        }

        try {
            return sendMessage0(message);
        } finally {
            callers.remove(Thread.currentThread());
        }
    }

    protected boolean sendMessage0(GelfMessage message) {

        try {

            redisClientWrapper
                    .getRedisClient()
                    .lpush(redisKey, message.toJson(""));

            return true;
        } catch (Exception e) {
            errorReporter.reportError(e.getMessage(), new IOException("Cannot send REDIS data with key URI " + redisKey, e));
            return false;
        }
    }

    public void close() {
        callers.clear();
        redisClientWrapper.close();
    }
}
