package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.contrib.providers.flagd.Config;
import dev.openfeature.contrib.providers.flagd.FlagdOptions;
import dev.openfeature.contrib.providers.flagd.FlagdProvider;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoBreakingChangesTest {

    private AtomicBoolean isTestRunning;
    private ConcurrentLinkedQueue<Throwable> uncaughtExceptions;
    private Thread threadWatcher;

    @BeforeEach
    void setup() {
        final var isRunning = new AtomicBoolean(true);
        final var uncaught = new ConcurrentLinkedQueue<Throwable>();
        uncaughtExceptions = uncaught;
        isTestRunning = isRunning;

        threadWatcher = new Thread(() -> {
            var seenThreads = new HashSet<Thread>();
            while (isRunning.get()) {
                var stacks = Thread.getAllStackTraces();
                for (var entry : stacks.entrySet()) {
                    var thread = entry.getKey();
                    if (seenThreads.add(thread)) {
                        thread.setUncaughtExceptionHandler((thread1, throwable) -> {
                            uncaught.add(throwable);
                        });
                    }
                }
            }
        });
        threadWatcher.setDaemon(true);
        threadWatcher.start();
    }

    @Test
    void noBreakingChanges() throws InterruptedException {
        try {
            var testProvider = new FlagdProvider(FlagdOptions.builder()
                    .resolverType(Config.Resolver.FILE)
                    .offlineFlagSourcePath(NoBreakingChangesTest.class
                            .getResource("/testFlags.json")
                            .getPath())
                    .build());
            var api = new OpenFeatureAPI();
            api.setProviderAndWait(testProvider);

            var client = api.getClient();
            var flagFound = client.getBooleanDetails("basic-boolean", false);
            assertThat(flagFound).isNotNull();
            assertThat(flagFound.getValue()).isTrue();
            assertThat(flagFound.getVariant()).isEqualTo("true");
            assertThat(flagFound.getReason()).isEqualTo(Reason.STATIC.toString());

            var flagNotFound = client.getStringDetails("unknown", "asd");
            assertThat(flagNotFound).isNotNull();
            assertThat(flagNotFound.getValue()).isEqualTo("asd");
            assertThat(flagNotFound.getVariant()).isNull();
            assertThat(flagNotFound.getReason()).isEqualTo(Reason.ERROR.toString());
            assertThat(flagNotFound.getErrorCode()).isEqualTo(ErrorCode.FLAG_NOT_FOUND);

            testProvider.shutdown();
            api.shutdown();

        } finally {
            try {
                Thread.sleep(1000); // wait a bit for any uncaught exceptions to be reported

                isTestRunning.set(false);
                threadWatcher.join(1000);
            } finally {
                assertThat(uncaughtExceptions).isEmpty();
            }
        }
    }
}
