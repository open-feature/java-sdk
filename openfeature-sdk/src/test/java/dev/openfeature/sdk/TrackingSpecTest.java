package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import dev.openfeature.api.Client;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.FeatureProvider;
import dev.openfeature.api.ImmutableContext;
import dev.openfeature.api.ImmutableStructure;
import dev.openfeature.api.ImmutableTrackingEventDetails;
import dev.openfeature.api.MutableContext;
import dev.openfeature.api.MutableTrackingEventDetails;
import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.api.TrackingEventDetails;
import dev.openfeature.api.Value;
import dev.openfeature.sdk.fixtures.ProviderFixture;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrackingSpecTest {

    private OpenFeatureAPI api;
    private Client client;

    @BeforeEach
    void getApiInstance() {
        api = new DefaultOpenFeatureAPI();
        client = api.getClient();
    }

    @Specification(
            number = "6.1.1.1",
            text = "The `client` MUST define a function for tracking the occurrence of "
                    + "a particular action or application state, with parameters `tracking event name` (string, required), "
                    + "`evaluation context` (optional) and `tracking event details` (optional), which returns nothing.")
    @Specification(
            number = "6.1.2.1",
            text = "The `client` MUST define a function for tracking the occurrence of a "
                    + "particular action or application state, with parameters `tracking event name` (string, required) and "
                    + "`tracking event details` (optional), which returns nothing.")
    @Test
    void trackMethodFulfillsSpec() throws Exception {

        ImmutableContext ctx = new ImmutableContext();
        MutableTrackingEventDetails details = new MutableTrackingEventDetails(0.0f);
        assertThatCode(() -> client.track("event")).doesNotThrowAnyException();
        assertThatCode(() -> client.track("event", ctx)).doesNotThrowAnyException();
        assertThatCode(() -> client.track("event", details)).doesNotThrowAnyException();
        assertThatCode(() -> client.track("event", ctx, details)).doesNotThrowAnyException();

        assertThrows(NullPointerException.class, () -> client.track(null, ctx, details));
        assertThrows(NullPointerException.class, () -> client.track("event", null, details));
        assertThrows(NullPointerException.class, () -> client.track("event", ctx, null));
        assertThrows(NullPointerException.class, () -> client.track(null, null, null));
        assertThrows(NullPointerException.class, () -> client.track(null, ctx));
        assertThrows(NullPointerException.class, () -> client.track(null, details));
        assertThrows(NullPointerException.class, () -> client.track("event", (EvaluationContext) null));
        assertThrows(NullPointerException.class, () -> client.track("event", (TrackingEventDetails) null));

        assertThrows(IllegalArgumentException.class, () -> client.track(""));
        assertThrows(IllegalArgumentException.class, () -> client.track("", ctx));
        assertThrows(IllegalArgumentException.class, () -> client.track("", ctx, details));

        Class<OpenFeatureClient> clientClass = OpenFeatureClient.class;
        assertEquals(
                void.class,
                clientClass.getMethod("track", String.class).getReturnType(),
                "The method should return void.");
        assertEquals(
                void.class,
                clientClass
                        .getMethod("track", String.class, EvaluationContext.class)
                        .getReturnType(),
                "The method should return void.");

        assertEquals(
                void.class,
                clientClass
                        .getMethod("track", String.class, EvaluationContext.class, TrackingEventDetails.class)
                        .getReturnType(),
                "The method should return void.");
    }

    @Specification(
            number = "6.1.3",
            text = "The evaluation context passed to the provider's track function "
                    + "MUST be merged in the order: API (global; lowest precedence) -> transaction -> client -> "
                    + "invocation (highest precedence), with duplicate values being overwritten.")
    @Test
    void contextsGetMerged() {

        api.setTransactionContextPropagator(new ThreadLocalTransactionContextPropagator());

        Map<String, Value> apiAttr = new HashMap<>();
        apiAttr.put("my-key", new Value("hey"));
        apiAttr.put("my-api-key", new Value("333"));
        EvaluationContext apiCtx = new ImmutableContext(apiAttr);
        api.setEvaluationContext(apiCtx);

        Map<String, Value> txAttr = new HashMap<>();
        txAttr.put("my-key", new Value("overwritten"));
        txAttr.put("my-tx-key", new Value("444"));
        EvaluationContext txCtx = new ImmutableContext(txAttr);
        api.setTransactionContext(txCtx);

        Map<String, Value> clAttr = new HashMap<>();
        clAttr.put("my-key", new Value("overwritten-again"));
        clAttr.put("my-cl-key", new Value("555"));
        EvaluationContext clCtx = new ImmutableContext(clAttr);
        client.setEvaluationContext(clCtx);

        FeatureProvider provider = ProviderFixture.createMockedProvider();
        api.setProviderAndWait(provider);

        client.track("event", new MutableContext().add("my-key", "final"), new MutableTrackingEventDetails(0.0f));

        Map<String, Value> expectedMap = Maps.newHashMap();
        expectedMap.put("my-key", new Value("final"));
        expectedMap.put("my-api-key", new Value("333"));
        expectedMap.put("my-tx-key", new Value("444"));
        expectedMap.put("my-cl-key", new Value("555"));
        verify(provider).track(eq("event"), argThat(ctx -> ctx.asMap().equals(expectedMap)), notNull());
    }

    @Specification(
            number = "6.1.4",
            text = "If the client's `track` function is called and the associated provider "
                    + "does not implement tracking, the client's `track` function MUST no-op.")
    @Test
    void noopProvider() {
        FeatureProvider provider = spy(FeatureProvider.class);
        api.setProvider(provider);
        client.track("event");
        verify(provider).track(any(), any(), any());
    }

    @Specification(
            number = "6.2.1",
            text = "The `tracking event details` structure MUST define an optional numeric "
                    + "`value`, associating a scalar quality with an `tracking event`.")
    @Specification(
            number = "6.2.2",
            text =
                    "The `tracking event details` MUST support the inclusion of custom "
                            + "fields, having keys of type `string`, and values of type `boolean | string | number | structure`.")
    @Test
    void eventDetails() {
        assertFalse(new MutableTrackingEventDetails().getValue().isPresent());
        assertFalse(new ImmutableTrackingEventDetails().getValue().isPresent());
        assertThat(new ImmutableTrackingEventDetails(2).getValue()).hasValue(2);
        assertThat(new MutableTrackingEventDetails(9.87f).getValue()).hasValue(9.87f);

        // using mutable tracking event details
        Map<String, Value> expectedMap = Maps.newHashMap();
        expectedMap.put("my-str", new Value("str"));
        expectedMap.put("my-num", new Value(1));
        expectedMap.put("my-bool", new Value(true));
        expectedMap.put("my-struct", new Value(new MutableTrackingEventDetails()));

        MutableTrackingEventDetails details = new MutableTrackingEventDetails()
                .add("my-str", new Value("str"))
                .add("my-num", new Value(1))
                .add("my-bool", new Value(true))
                .add("my-struct", new Value(new MutableTrackingEventDetails()));

        assertEquals(expectedMap, details.asMap());
        assertThatCode(() -> api.getClient()
                        .track("tracking-event-name", new ImmutableContext(), new MutableTrackingEventDetails()))
                .doesNotThrowAnyException();

        // using immutable tracking event details
        ImmutableMap<String, Value> expectedImmutable = ImmutableMap.of(
                "my-str",
                new Value("str"),
                "my-num",
                new Value(1),
                "my-bool",
                new Value(true),
                "my-struct",
                new Value(new ImmutableStructure()));

        ImmutableTrackingEventDetails immutableDetails = new ImmutableTrackingEventDetails(2, expectedMap);
        assertEquals(expectedImmutable, immutableDetails.asMap());
        assertThatCode(() -> api.getClient()
                        .track("tracking-event-name", new ImmutableContext(), new ImmutableTrackingEventDetails()))
                .doesNotThrowAnyException();
    }
}
