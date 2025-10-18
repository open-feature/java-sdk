package dev.openfeature.sdk.testutils.testProvider;

import dev.openfeature.sdk.*;
import dev.openfeature.sdk.e2e.Flag;
import dev.openfeature.sdk.exceptions.FatalError;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestProvider extends EventProvider {
    public static final String DEFAULT_VARIANT = "Passed in default";

    private final String name;
    private final List<Hook> hooks;
    private final int initDelay;
    private final Awaitable initWaitsFor;
    private final Map<String, Flag> flags;
    private final boolean fatalOnInit;
    private final boolean errorOnInit;
    private final boolean errorsOnFlagEvaluation;
    private final ErrorCode errorCode;
    private final String errorMessage;
    private final RuntimeException throwable;
    private final ConcurrentLinkedQueue<FlagEvaluation> flagEvaluations = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final boolean allowAnyFlag;

    private TestProvider(
            String name,
            List<Hook> hooks,
            int initDelay,
            Awaitable initWaitsFor,
            boolean allowAnyFlag,
            Map<String, Flag> flags,
            boolean errorsOnFlagEvaluation,
            boolean errorOnInit,
            ErrorCode errorCode,
            String errorMessage,
            RuntimeException throwable,
            boolean fatalOnInit) {
        this.name = name == null ? "TestProvider" : name;
        this.hooks = hooks;
        this.initDelay = initDelay;
        this.initWaitsFor = initWaitsFor;
        this.allowAnyFlag = allowAnyFlag;
        this.flags = flags;
        this.errorsOnFlagEvaluation = errorsOnFlagEvaluation;
        this.errorOnInit = errorOnInit;
        this.errorCode = errorCode == null ? ErrorCode.GENERAL : errorCode;
        this.errorMessage = errorMessage == null ? "Test error" : errorMessage;
        this.throwable = throwable;
        this.fatalOnInit = fatalOnInit;
    }

    public List<FlagEvaluation> getFlagEvaluations() {
        return new ArrayList<>(flagEvaluations);
    }

    @Override
    public List<Hook> getProviderHooks() {
        return hooks;
    }

    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        if (initWaitsFor != null) {
            initWaitsFor.await();
        } else if (initDelay > 0) {
            var end = System.currentTimeMillis() + initDelay;
            long delta = initDelay;
            while (delta > 0) {
                try {
                    Thread.sleep(delta);
                } catch (InterruptedException e) {
                    // ignore
                }
                delta = end - System.currentTimeMillis();
            }
        }

        if (fatalOnInit) {
            throw new FatalError("TestProvider is set to fatal state, thus will throw on init");
        }
        if (errorOnInit) {
            throw new RuntimeException("TestProvider is set to error state, thus will throw on init");
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        isShutdown.set(true);
    }

    public boolean isShutdown() {
        return isShutdown.get();
    }

    @Override
    public Metadata getMetadata() {
        return () -> name;
    }

    private <T> ProviderEvaluation<T> getEvaluation(
            String key, T defaultValue, FlagValueType flagType, Class<T> clazz, EvaluationContext evaluationContext) {
        flagEvaluations.add(new FlagEvaluation(key, flagType, evaluationContext));
        if (throwable != null) {
            throw throwable;
        }
        var builder = ProviderEvaluation.<T>builder();
        if (errorsOnFlagEvaluation) {
            return builder.errorMessage(errorMessage).errorCode(errorCode).build();
        }
        if (allowAnyFlag) {
            return builder.reason(Reason.STATIC.name())
                    .value(clazz.cast(defaultValue))
                    .flagMetadata(ImmutableMetadata.EMPTY)
                    .variant(DEFAULT_VARIANT)
                    .build();
        }
        var flag = flags.get(key);
        if (flag == null) {
            return builder.errorCode(ErrorCode.FLAG_NOT_FOUND)
                    .errorMessage("Flag not found")
                    .build();
        }
        if (flagType.name().equals(flag.type)) {
            return builder.reason(Reason.STATIC.name())
                    .value(clazz.cast(flag.defaultValue))
                    .flagMetadata(flag.flagMetadata)
                    .variant(DEFAULT_VARIANT)
                    .build();
        }
        return builder.errorCode(ErrorCode.TYPE_MISMATCH)
                .errorMessage("Flag type mismatch")
                .flagMetadata(flag.flagMetadata)
                .build();
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getEvaluation(key, defaultValue, FlagValueType.BOOLEAN, Boolean.class, ctx);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        return getEvaluation(key, defaultValue, FlagValueType.STRING, String.class, ctx);
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        return getEvaluation(key, defaultValue, FlagValueType.INTEGER, Integer.class, ctx);
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        return getEvaluation(key, defaultValue, FlagValueType.DOUBLE, Double.class, ctx);
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        return getEvaluation(key, defaultValue, FlagValueType.OBJECT, Value.class, ctx);
    }

    public interface PostInit {
        TestProvider initsToReady();

        TestProvider initsToError(boolean isError);

        default TestProvider initsToError() {
            return initsToError(true);
        }

        TestProvider initsToFatal(boolean isFatal);

        default TestProvider initsToFatal() {
            return initsToFatal(true);
        }

        TestProvider withExceptionOnFlagEvaluation(RuntimeException runtimeException);

        default TestProvider withExceptionOnFlagEvaluation() {
            return withExceptionOnFlagEvaluation(new FlagNotFoundError(TestConstants.BROKEN_MESSAGE));
        }
    }

    public interface InitConfig {
        PostInit initWaitsFor(int millis);

        PostInit initWaitsFor(Awaitable awaitable);
    }

    public interface WithFlags extends InitConfig, PostInit {}

    public interface FlagConfig {
        FlagConfig withFlag(Flag flag);

        WithFlags withFlags(Flag... flags);

        WithFlags withFlags(Iterable<Flag> flags);

        default WithFlags errorsOnFlagEvaluation() {
            return errorsOnFlagEvaluation(true);
        }

        WithFlags errorsOnFlagEvaluation(boolean error);

        WithFlags errorsOnFlagEvaluation(ErrorCode errorCode);

        WithFlags errorsOnFlagEvaluation(ErrorCode errorCode, String errorMessage);

        default WithFlags allowUnknownFlags() {
            return allowUnknownFlags(true);
        }

        WithFlags allowUnknownFlags(boolean allowEveryRequestedFlag);
    }

    public interface WithHooks extends FlagConfig, WithFlags {}

    public interface HookConfig {
        Builder withHook(Hook hook);

        WithHooks withHooks(Hook... hooks);

        WithHooks withHooks(Iterable<Hook> hooks);
    }

    public interface WithName extends WithHooks, HookConfig {}

    public interface NameConfig {
        WithName withName(String name);
    }

    public interface Builder extends WithName, NameConfig {}

    public static Builder builder() {
        return new TestProviderBuilder();
    }

    public static class TestProviderBuilder implements Builder {
        private final List<Hook> hooks = new ArrayList<>();
        private final Map<String, Flag> flags = new HashMap<>();
        private Awaitable initWaitsFor;
        private int initDelay = 0;
        private boolean errorsOnFlagEvaluation;
        private ErrorCode errorCode;
        private String errorMessage;
        private RuntimeException runtimeException;
        private boolean errorOnInit = false;
        private boolean fatalOnInit = false;
        private boolean allowAnyFlag = false;
        private String name;

        @Override
        public WithName withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder withHook(Hook hook) {
            this.hooks.add(hook);
            return this;
        }

        @Override
        public WithHooks withHooks(Hook... hooks) {
            for (int i = 0; i < hooks.length; i++) {
                this.hooks.add(hooks[i]);
            }
            return this;
        }

        @Override
        public WithHooks withHooks(Iterable<Hook> hooks) {
            for (Hook hook : hooks) {
                this.hooks.add(hook);
            }
            return this;
        }

        @Override
        public WithFlags errorsOnFlagEvaluation(boolean error) {
            this.errorsOnFlagEvaluation = error;
            return this;
        }

        @Override
        public WithFlags errorsOnFlagEvaluation(ErrorCode errorCode) {
            this.errorsOnFlagEvaluation = true;
            this.errorCode = errorCode;
            return this;
        }

        @Override
        public WithFlags errorsOnFlagEvaluation(ErrorCode errorCode, String errorMessage) {
            this.errorsOnFlagEvaluation = true;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            return this;
        }

        @Override
        public WithFlags allowUnknownFlags(boolean allowEveryRequestedFlag) {
            this.allowAnyFlag = allowEveryRequestedFlag;
            return this;
        }

        @Override
        public FlagConfig withFlag(Flag flag) {
            flags.put(flag.name, flag);
            return this;
        }

        @Override
        public WithFlags withFlags(Flag... flags) {
            for (Flag flag : flags) {
                this.flags.put(flag.name, flag);
            }
            return this;
        }

        @Override
        public WithFlags withFlags(Iterable<Flag> flags) {
            for (Flag flag : flags) {
                this.flags.put(flag.name, flag);
            }
            return this;
        }

        @Override
        public PostInit initWaitsFor(int millis) {
            initDelay = millis;
            initWaitsFor = null;
            return this;
        }

        @Override
        public PostInit initWaitsFor(Awaitable awaitable) {
            initDelay = 0;
            initWaitsFor = awaitable;
            return this;
        }

        @Override
        public TestProvider initsToReady() {
            errorOnInit = false;
            return build();
        }

        @Override
        public TestProvider initsToError(boolean isError) {
            this.errorOnInit = isError;
            return build();
        }

        @Override
        public TestProvider initsToError() {
            errorOnInit = true;
            return build();
        }

        @Override
        public TestProvider initsToFatal(boolean isFatal) {
            this.fatalOnInit = isFatal;
            return build();
        }

        @Override
        public TestProvider withExceptionOnFlagEvaluation(RuntimeException runtimeException) {
            this.runtimeException = runtimeException;
            return build();
        }

        private TestProvider build() {
            return new TestProvider(
                    name,
                    hooks,
                    initDelay,
                    initWaitsFor,
                    allowAnyFlag,
                    flags,
                    errorsOnFlagEvaluation,
                    errorOnInit,
                    errorCode,
                    errorMessage,
                    runtimeException,
                    fatalOnInit);
        }
    }
}
