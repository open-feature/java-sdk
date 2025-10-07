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
    private final ImmutableMetadata flagMetadata;
    private final boolean errorOnInit;
    private final boolean errorsOnFlagEvaluation;
    private final ErrorCode errorCode;
    private final String errorMessage;
    private final RuntimeException throwable;
    private final boolean isFatal;
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
            ImmutableMetadata flagMetadata,
            boolean errorOnInit,
            ErrorCode errorCode,
            String errorMessage,
            RuntimeException throwable,
            boolean isFatal) {
        this.name = name == null ? "TestProvider" : name;
        this.hooks = hooks;
        this.initDelay = initDelay;
        this.initWaitsFor = initWaitsFor;
        this.allowAnyFlag = allowAnyFlag;
        this.flags = flags;
        this.errorsOnFlagEvaluation = errorsOnFlagEvaluation;
        this.flagMetadata = flagMetadata;
        this.errorOnInit = errorOnInit;
        this.errorCode = errorCode == null ? ErrorCode.GENERAL : errorCode;
        this.errorMessage = errorMessage == null ? "Test error" : errorMessage;
        this.throwable = throwable;
        this.isFatal = isFatal;
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
            var start = System.currentTimeMillis();
            long delta = System.currentTimeMillis() - start;
            while (delta > 0) {
                try {
                    Thread.sleep(delta);
                } catch (InterruptedException e) {
                    // ignore
                }
                delta = System.currentTimeMillis() - start;
            }
        }

        if (isFatal) {
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
                    .flagMetadata(flagMetadata)
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
                    .flagMetadata(flagMetadata)
                    .variant(DEFAULT_VARIANT)
                    .build();
        }
        return builder.errorCode(ErrorCode.TYPE_MISMATCH)
                .errorMessage("Flag type mismatch")
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

    public interface FlagConfig {
        Builder withName(String name);

        Builder withHook(Hook hook);

        Builder withHooks(Hook... hooks);

        Builder withHooks(Iterable<Hook> hooks);

        default Builder errorsOnFlagEvaluation() {
            return errorsOnFlagEvaluation(true);
        }

        Builder errorsOnFlagEvaluation(boolean error);

        Builder errorsOnFlagEvaluation(ErrorCode errorCode);

        Builder errorsOnFlagEvaluation(ErrorCode errorCode, String errorMessage);

        default Builder allowUnknownFlags() {
            return allowUnknownFlags(true);
        }

        Builder allowUnknownFlags(boolean allowEveryRequestedFlag);

        Builder withFlag(Flag flag);

        WithFlags withFlags(Flag... flags);

        WithFlags withFlags(Iterable<Flag> flags);

        WithFlags withFlagMetadata(ImmutableMetadata flagMetadata);
    }

    public interface InitConfig {
        PostInit initWaitsFor(int millis);

        PostInit initWaitsFor(Awaitable awaitable);
    }

    public interface PostInit {
        TestProvider isReady();

        TestProvider isInError(boolean isError);

        default TestProvider isInError() {
            return isInError(true);
        }

        TestProvider isFatal(boolean isFatal);

        default TestProvider isFatal() {
            return isFatal(true);
        }

        TestProvider withExceptionOnFlagEvaluation(RuntimeException runtimeException);

        TestProvider withExceptionOnFlagEvaluation();
    }

    public interface WithFlags extends InitConfig, PostInit {}

    public interface Builder extends FlagConfig, WithFlags {}

    public static Builder builder() {
        return new TestProviderBuilder();
    }

    public static class TestProviderBuilder implements Builder {
        private final List<Hook> hooks = new ArrayList<>();
        private final Map<String, Flag> flags = new HashMap<>();
        private Awaitable initWaitsFor;
        private ErrorCode errorCode;
        private String errorMessage;
        private RuntimeException runtimeException;
        private boolean errorOnInit = false;
        private boolean allowAnyFlag = false;
        private boolean isFatal = false;
        private int initDelay = 0;
        private ImmutableMetadata flagMetadata = ImmutableMetadata.EMPTY;
        private boolean errorsOnFlagEvaluation;
        private String name;

        @Override
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder withHook(Hook hook) {
            this.hooks.add(hook);
            return this;
        }

        @Override
        public Builder withHooks(Hook... hooks) {
            for (int i = 0; i < hooks.length; i++) {
                this.hooks.add(hooks[i]);
            }
            return this;
        }

        @Override
        public Builder withHooks(Iterable<Hook> hooks) {
            for (Hook hook : hooks) {
                this.hooks.add(hook);
            }
            return this;
        }

        @Override
        public Builder errorsOnFlagEvaluation(boolean error) {
            this.errorsOnFlagEvaluation = error;
            return this;
        }

        @Override
        public Builder errorsOnFlagEvaluation(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        @Override
        public Builder errorsOnFlagEvaluation(ErrorCode errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            return this;
        }

        @Override
        public Builder allowUnknownFlags(boolean allowEveryRequestedFlag) {
            this.allowAnyFlag = allowEveryRequestedFlag;
            return this;
        }

        @Override
        public Builder withFlag(Flag flag) {
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
        public WithFlags withFlagMetadata(ImmutableMetadata flagMetadata) {
            this.flagMetadata = flagMetadata;
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
            initWaitsFor = awaitable;
            return this;
        }

        @Override
        public TestProvider isReady() {
            errorOnInit = false;
            return build();
        }

        @Override
        public TestProvider isInError(boolean isError) {
            this.errorOnInit = isError;
            return build();
        }

        @Override
        public TestProvider isInError() {
            errorOnInit = true;
            return build();
        }

        @Override
        public TestProvider isFatal(boolean isFatal) {
            this.isFatal = isFatal;
            return build();
        }

        @Override
        public TestProvider withExceptionOnFlagEvaluation(RuntimeException runtimeException) {
            this.runtimeException = runtimeException;
            return build();
        }

        @Override
        public TestProvider withExceptionOnFlagEvaluation() {
            this.runtimeException = new FlagNotFoundError(TestConstants.BROKEN_MESSAGE);
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
                    flagMetadata,
                    errorOnInit,
                    errorCode,
                    errorMessage,
                    runtimeException,
                    isFatal);
        }
    }
}
