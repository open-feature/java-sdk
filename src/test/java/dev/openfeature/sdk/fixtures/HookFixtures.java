package dev.openfeature.sdk.fixtures;

import static org.mockito.Mockito.spy;

import dev.openfeature.sdk.BooleanHook;
import dev.openfeature.sdk.DoubleHook;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.IntegerHook;
import dev.openfeature.sdk.LongHook;
import dev.openfeature.sdk.ObjectHook;
import dev.openfeature.sdk.StringHook;
import java.util.Map;

public interface HookFixtures {

    // the SDK only invokes the error stage of hooks that implement it, so these fixtures override it

    default Hook<Boolean> mockBooleanHook() {
        return spy(new BooleanHook() {
            @Override
            public void error(HookContext<Boolean> ctx, Exception error, Map<String, Object> hints) {}
        });
    }

    default Hook<String> mockStringHook() {
        return spy(new StringHook() {
            @Override
            public void error(HookContext<String> ctx, Exception error, Map<String, Object> hints) {}
        });
    }

    default Hook<Integer> mockIntegerHook() {
        return spy(new IntegerHook() {
            @Override
            public void error(HookContext<Integer> ctx, Exception error, Map<String, Object> hints) {}
        });
    }

    default Hook<Long> mockLongHook() {
        return spy(new LongHook() {
            @Override
            public void error(HookContext<Long> ctx, Exception error, Map<String, Object> hints) {}
        });
    }

    default Hook<Double> mockDoubleHook() {
        return spy(new DoubleHook() {
            @Override
            public void error(HookContext<Double> ctx, Exception error, Map<String, Object> hints) {}
        });
    }

    default Hook<Object> mockObjectHook() {
        return spy(new ObjectHook() {
            @Override
            public void error(HookContext<Object> ctx, Exception error, Map<String, Object> hints) {}
        });
    }

    default Hook<?> mockGenericHook() {
        return spy(new Hook<Object>() {
            @Override
            public void error(HookContext<Object> ctx, Exception error, Map<String, Object> hints) {}
        });
    }
}
