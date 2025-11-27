package dev.openfeature.sdk;

import dev.openfeature.contrib.providers.flagd.Config;
import dev.openfeature.contrib.providers.flagd.FlagdOptions;
import dev.openfeature.contrib.providers.flagd.FlagdProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

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

    @AfterEach
    void teardown() throws InterruptedException {
        try {
            Thread.sleep(1000); // wait a bit for any uncaught exceptions to be reported

            isTestRunning.set(false);
            threadWatcher.join(1000);
        } finally {
            assertThat(uncaughtExceptions).isEmpty();
        }
    }

    @Test
    void noBreakingChanges() throws IOException {

        var file = new File("testFlags.json");
        file.mkdirs();
        file.createNewFile();
        file.deleteOnExit();

        System.err.println("Using flag file at: " + file.getAbsolutePath());

        var writer = new BufferedWriter(new FileWriter(file));
        writer.write("{\n" +
                "  \"$schema\": \"https://flagd.dev/schema/v0/flags.json\",\n" +
                "  \"flags\": {\n" +
                "    \"basic-boolean\": {\n" +
                "      \"state\": \"ENABLED\",\n" +
                "      \"defaultVariant\": \"true\",\n" +
                "      \"variants\": {\n" +
                "        \"true\": true,\n" +
                "        \"false\": false\n" +
                "      },\n" +
                "      \"targeting\": {}\n" +
                "    }\n" +
                "  }\n" +
                "}\n");
        writer.flush();

        System.err.println("written to file");

        var testProvider = new FlagdProvider(FlagdOptions.builder()
                .resolverType(Config.Resolver.FILE)
                .offlineFlagSourcePath(file.getAbsolutePath())
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
    }
}
