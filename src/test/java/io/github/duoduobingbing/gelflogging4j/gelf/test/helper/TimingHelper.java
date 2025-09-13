package io.github.duoduobingbing.gelflogging4j.gelf.test.helper;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class TimingHelper {

    public static void waitUntil(Supplier<Boolean> condition, long timeOutValue, ChronoUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            long deadline = System.currentTimeMillis() + Duration.of(timeOutValue, timeUnit).toMillis();
            while (System.currentTimeMillis() < deadline + 100) {
                if (condition.get()) {
                    return;
                }
                try {
                    Thread.sleep(100); // polling interval
                } catch (InterruptedException ignored) {

                }
            }

            throw new IllegalStateException("Timeout while waiting for condition");
        });

        future.get(timeOutValue, TimeUnit.of(timeUnit));
    }

}
