package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import dev.openfeature.sdk.testutils.TestEventsProvider;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

class EventsTest {

    private static final int TIMEOUT = 500;
    private static final int INIT_DELAY = TIMEOUT / 2;
    private OpenFeatureAPI api;

    @BeforeEach
    void setUp() {
        api = new OpenFeatureAPI();
    }

    @Nested
    class ApiEvents {

        @Nested
        @DisplayName("named provider")
        class NamedProvider {

            @Nested
            @DisplayName("initialization")
            class Initialization {

                @Test
                @DisplayName("should fire initial READY event when provider init succeeds")
                @Specification(
                        number = "5.3.1",
                        text = "If the provider's initialize function terminates normally,"
                                + " PROVIDER_READY handlers MUST run.")
                void apiInitReady() {
                    final Consumer<EventDetails> handler = mockHandler();
                    final String name = "apiInitReady";

                    TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                    api.onProviderReady(handler);
                    api.setProviderAndWait(name, provider);
                    verify(handler, timeout(TIMEOUT).atLeastOnce()).accept(any());
                }

                @Test
                @DisplayName("should fire initial ERROR event when provider init errors")
                @Specification(
                        number = "5.3.2",
                        text = "If the provider's initialize function terminates abnormally,"
                                + " PROVIDER_ERROR handlers MUST run.")
                void apiInitError() {
                    final Consumer<EventDetails> handler = mockHandler();
                    final String name = "apiInitError";
                    final String errMessage = "oh no!";

                    TestEventsProvider provider = new TestEventsProvider(INIT_DELAY, true, errMessage);
                    api.onProviderError(handler);
                    api.setProvider(name, provider);
                    verify(handler, timeout(TIMEOUT)).accept(argThat(details -> {
                        return errMessage.equals(details.getMessage());
                    }));
                }
            }

            @Nested
            @DisplayName("provider events")
            class ProviderEvents {

                @Test
                @DisplayName("should propagate events")
                @Specification(
                        number = "5.1.2",
                        text = "When a provider signals the occurrence of a particular event, "
                                + "the associated client and API event handlers MUST run.")
                void apiShouldPropagateEvents() {
                    final Consumer<EventDetails> handler = mockHandler();
                    final String name = "apiShouldPropagateEvents";

                    TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                    api.setProviderAndWait(name, provider);
                    api.onProviderConfigurationChanged(handler);

                    provider.mockEvent(
                            ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                            EventDetails.builder().build());
                    verify(handler, timeout(TIMEOUT)).accept(any());
                }

                @Test
                @DisplayName("should support all event types")
                @Specification(
                        number = "5.1.1",
                        text =
                                "The provider MAY define a mechanism for signaling the occurrence "
                                        + "of one of a set of events, including PROVIDER_READY, PROVIDER_ERROR, "
                                        + "PROVIDER_CONFIGURATION_CHANGED and PROVIDER_STALE, with a provider event details payload.")
                @Specification(
                        number = "5.2.2",
                        text = "The API MUST provide a function for associating handler functions"
                                + " with a particular provider event type.")
                void apiShouldSupportAllEventTypes() {
                    final String name = "apiShouldSupportAllEventTypes";
                    final Consumer<EventDetails> handler1 = mockHandler();
                    final Consumer<EventDetails> handler2 = mockHandler();
                    final Consumer<EventDetails> handler3 = mockHandler();
                    final Consumer<EventDetails> handler4 = mockHandler();

                    TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                    api.setProviderAndWait(name, provider);

                    api.onProviderReady(handler1);
                    api.onProviderConfigurationChanged(handler2);
                    api.onProviderStale(handler3);
                    api.onProviderError(handler4);

                    Arrays.asList(ProviderEvent.values()).stream().forEach(eventType -> {
                        provider.mockEvent(
                                eventType, ProviderEventDetails.builder().build());
                    });

                    verify(handler1, timeout(TIMEOUT).atLeastOnce()).accept(any());
                    verify(handler2, timeout(TIMEOUT).atLeastOnce()).accept(any());
                    verify(handler3, timeout(TIMEOUT).atLeastOnce()).accept(any());
                    verify(handler4, timeout(TIMEOUT).atLeastOnce()).accept(any());
                }
            }
        }
    }

    @Nested
    @DisplayName("client events")
    class ClientEvents {

        @Nested
        @DisplayName("default provider")
        class DefaultProvider {

            @Nested
            @DisplayName("provider events")
            class ProviderEvents {

                @Test
                @DisplayName("should propagate events for default provider and anonymous client")
                @Specification(
                        number = "5.1.2",
                        text =
                                "When a provider signals the occurrence of a particular event, the associated client and API event handlers MUST run.")
                void shouldPropagateDefaultAndAnon() {
                    final Consumer<EventDetails> handler = mockHandler();

                    TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                    // set provider before getting a client
                    api.setProviderAndWait(provider);
                    Client client = api.getClient();
                    client.onProviderStale(handler);

                    provider.mockEvent(
                            ProviderEvent.PROVIDER_STALE, EventDetails.builder().build());
                    verify(handler, timeout(TIMEOUT)).accept(any());
                }

                @Test
                @DisplayName("should propagate events for default provider and named client")
                @Specification(
                        number = "5.1.2",
                        text =
                                "When a provider signals the occurrence of a particular event, the associated client and API event handlers MUST run.")
                void shouldPropagateDefaultAndNamed() {
                    final Consumer<EventDetails> handler = mockHandler();
                    final String name = "shouldPropagateDefaultAndNamed";

                    TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                    // set provider before getting a client
                    api.setProviderAndWait(provider);
                    Client client = api.getClient(name);
                    client.onProviderStale(handler);

                    provider.mockEvent(
                            ProviderEvent.PROVIDER_STALE, EventDetails.builder().build());
                    verify(handler, timeout(TIMEOUT)).accept(any());
                }
            }
        }
    }

    @Nested
    @DisplayName("named provider")
    class NamedProvider {

        @Nested
        @DisplayName("initialization")
        class Initialization {
            @Test
            @DisplayName("should fire initial READY event when provider init succeeds after client retrieved")
            @Specification(
                    number = "5.3.1",
                    text =
                            "If the provider's initialize function terminates normally, PROVIDER_READY handlers MUST run.")
            void initReadyProviderBefore() {
                final Consumer<EventDetails> handler = mockHandler();
                final String name = "initReadyProviderBefore";

                TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                Client client = api.getClient(name);
                client.onProviderReady(handler);
                // set provider after getting a client
                api.setProviderAndWait(name, provider);
                verify(handler, timeout(TIMEOUT).atLeastOnce())
                        .accept(argThat(details -> details.getDomain().equals(name)));
            }

            @Test
            @DisplayName("should fire initial READY event when provider init succeeds before client retrieved")
            @Specification(
                    number = "5.3.1",
                    text =
                            "If the provider's initialize function terminates normally, PROVIDER_READY handlers MUST run.")
            void initReadyProviderAfter() {
                final Consumer<EventDetails> handler = mockHandler();
                final String name = "initReadyProviderAfter";

                TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                // set provider before getting a client
                api.setProviderAndWait(name, provider);
                Client client = api.getClient(name);
                client.onProviderReady(handler);
                verify(handler, timeout(TIMEOUT).atLeastOnce())
                        .accept(argThat(details -> details.getDomain().equals(name)));
            }

            @Test
            @DisplayName("should fire initial ERROR event when provider init errors after client retrieved")
            @Specification(
                    number = "5.3.2",
                    text =
                            "If the provider's initialize function terminates abnormally, PROVIDER_ERROR handlers MUST run.")
            void initErrorProviderAfter() {
                final Consumer<EventDetails> handler = mockHandler();
                final String name = "initErrorProviderAfter";
                final String errMessage = "oh no!";

                TestEventsProvider provider = new TestEventsProvider(INIT_DELAY, true, errMessage);
                Client client = api.getClient(name);
                client.onProviderError(handler);
                // set provider after getting a client
                api.setProvider(name, provider);
                verify(handler, timeout(TIMEOUT)).accept(argThat(details -> {
                    return name.equals(details.getDomain()) && errMessage.equals(details.getMessage());
                }));
            }

            @Test
            @DisplayName("should fire initial ERROR event when provider init errors before client retrieved")
            @Specification(
                    number = "5.3.2",
                    text =
                            "If the provider's initialize function terminates abnormally, PROVIDER_ERROR handlers MUST run.")
            void initErrorProviderBefore() {
                final Consumer<EventDetails> handler = mockHandler();
                final String name = "initErrorProviderBefore";
                final String errMessage = "oh no!";

                TestEventsProvider provider = new TestEventsProvider(INIT_DELAY, true, errMessage);
                // set provider after getting a client
                api.setProvider(name, provider);
                Client client = api.getClient(name);
                client.onProviderError(handler);
                verify(handler, timeout(TIMEOUT)).accept(argThat(details -> {
                    return name.equals(details.getDomain()) && errMessage.equals(details.getMessage());
                }));
            }
        }

        @Nested
        @DisplayName("provider events")
        class ProviderEvents {

            @Test
            @DisplayName("should propagate events when provider set before client retrieved")
            @Specification(
                    number = "5.1.2",
                    text =
                            "When a provider signals the occurrence of a particular event, the associated client and API event handlers MUST run.")
            void shouldPropagateBefore() {
                final Consumer<EventDetails> handler = mockHandler();
                final String name = "shouldPropagateBefore";

                TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                // set provider before getting a client
                api.setProviderAndWait(name, provider);
                Client client = api.getClient(name);
                client.onProviderConfigurationChanged(handler);

                provider.mockEvent(
                        ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                        EventDetails.builder().build());
                verify(handler, timeout(TIMEOUT))
                        .accept(argThat(details -> details.getDomain().equals(name)));
            }

            @Test
            @DisplayName("should propagate events when provider set after client retrieved")
            @Specification(
                    number = "5.1.2",
                    text =
                            "When a provider signals the occurrence of a particular event, the associated client and API event handlers MUST run.")
            void shouldPropagateAfter() {

                final Consumer<EventDetails> handler = mockHandler();
                final String name = "shouldPropagateAfter";

                TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                Client client = api.getClient(name);
                client.onProviderConfigurationChanged(handler);
                // set provider after getting a client
                api.setProviderAndWait(name, provider);

                provider.mockEvent(
                        ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                        EventDetails.builder().build());
                verify(handler, timeout(TIMEOUT))
                        .accept(argThat(details -> details.getDomain().equals(name)));
            }

            @Test
            @DisplayName("should support all event types")
            @Specification(
                    number = "5.1.1",
                    text =
                            "The provider MAY define a mechanism for signaling the occurrence "
                                    + "of one of a set of events, including PROVIDER_READY, PROVIDER_ERROR, "
                                    + "PROVIDER_CONFIGURATION_CHANGED and PROVIDER_STALE, with a provider event details payload.")
            @Specification(
                    number = "5.2.1",
                    text = "The client MUST provide a function for associating handler functions"
                            + " with a particular provider event type.")
            void shouldSupportAllEventTypes() {
                final String name = "shouldSupportAllEventTypes";
                final Consumer<EventDetails> handler1 = mockHandler();
                final Consumer<EventDetails> handler2 = mockHandler();
                final Consumer<EventDetails> handler3 = mockHandler();
                final Consumer<EventDetails> handler4 = mockHandler();

                TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
                api.setProviderAndWait(name, provider);
                Client client = api.getClient(name);

                client.onProviderReady(handler1);
                client.onProviderConfigurationChanged(handler2);
                client.onProviderStale(handler3);
                client.onProviderError(handler4);

                Arrays.asList(ProviderEvent.values()).stream().forEach(eventType -> {
                    provider.mockEvent(eventType, ProviderEventDetails.builder().build());
                });
                ArgumentMatcher<EventDetails> nameMatches =
                        (EventDetails details) -> details.getDomain().equals(name);
                verify(handler1, timeout(TIMEOUT).atLeastOnce()).accept(argThat(nameMatches));
                verify(handler2, timeout(TIMEOUT).atLeastOnce()).accept(argThat(nameMatches));
                verify(handler3, timeout(TIMEOUT).atLeastOnce()).accept(argThat(nameMatches));
                verify(handler4, timeout(TIMEOUT).atLeastOnce()).accept(argThat(nameMatches));
            }
        }
    }

    @Test
    @DisplayName("shutdown provider should not run handlers")
    void shouldNotRunHandlers() {
        final Consumer<EventDetails> handler1 = mockHandler();
        final Consumer<EventDetails> handler2 = mockHandler();
        final String name = "shouldNotRunHandlers";

        TestEventsProvider provider1 = new TestEventsProvider(INIT_DELAY);
        TestEventsProvider provider2 = new TestEventsProvider(INIT_DELAY);
        api.setProviderAndWait(name, provider1);
        Client client = api.getClient(name);

        // attached handlers
        api.onProviderConfigurationChanged(handler1);
        client.onProviderConfigurationChanged(handler2);

        api.setProviderAndWait(name, provider2);

        // wait for the new provider to be ready and make sure things are cleaned up.
        await().until(() -> provider1.isShutDown());

        // fire old event
        provider1.mockEvent(
                ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                EventDetails.builder().build());

        // a bit of waiting here, but we want to make sure these are indeed never
        // called.
        verify(handler1, after(TIMEOUT).never()).accept(any());
        verify(handler2, never()).accept(any());
    }

    @Test
    @DisplayName("other client handlers should not run")
    @Specification(
            number = "5.1.3",
            text = "When a provider signals the occurrence of a particular event, "
                    + "event handlers on clients which are not associated with that provider MUST NOT run.")
    void otherClientHandlersShouldNotRun() {
        final String name1 = "otherClientHandlersShouldNotRun1";
        final String name2 = "otherClientHandlersShouldNotRun2";
        final Consumer<EventDetails> handlerToRun = mockHandler();
        final Consumer<EventDetails> handlerNotToRun = mockHandler();

        TestEventsProvider provider1 = new TestEventsProvider(INIT_DELAY);
        TestEventsProvider provider2 = new TestEventsProvider(INIT_DELAY);
        api.setProviderAndWait(name1, provider1);
        api.setProviderAndWait(name2, provider2);

        Client client1 = api.getClient(name1);
        Client client2 = api.getClient(name2);

        client1.onProviderConfigurationChanged(handlerToRun);
        client2.onProviderConfigurationChanged(handlerNotToRun);

        provider1.mockEvent(
                ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                ProviderEventDetails.builder().build());

        verify(handlerToRun, timeout(TIMEOUT)).accept(any());
        verify(handlerNotToRun, never()).accept(any());
    }

    @Test
    @DisplayName("bound named client handlers should not run with default")
    @Specification(
            number = "5.1.3",
            text = "When a provider signals the occurrence of a particular event, "
                    + "event handlers on clients which are not associated with that provider MUST NOT run.")
    void boundShouldNotRunWithDefault() {
        final String name = "boundShouldNotRunWithDefault";
        final Consumer<EventDetails> handlerNotToRun = mockHandler();

        TestEventsProvider namedProvider = new TestEventsProvider(INIT_DELAY);
        TestEventsProvider defaultProvider = new TestEventsProvider(INIT_DELAY);
        api.setProviderAndWait(defaultProvider);

        Client client = api.getClient(name);
        client.onProviderConfigurationChanged(handlerNotToRun);
        api.setProviderAndWait(name, namedProvider);

        // await the new provider to make sure the old one is shut down
        await().until(() -> namedProvider.getState().equals(ProviderState.READY));

        // fire event on default provider
        defaultProvider.mockEvent(
                ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                ProviderEventDetails.builder().build());

        verify(handlerNotToRun, after(TIMEOUT).never()).accept(any());
        api.setProviderAndWait(new NoOpProvider());
    }

    @Test
    @DisplayName("unbound named client handlers should run with default")
    @Specification(
            number = "5.1.3",
            text = "When a provider signals the occurrence of a particular event, "
                    + "event handlers on clients which are not associated with that provider MUST NOT run.")
    void unboundShouldRunWithDefault() {
        final String name = "unboundShouldRunWithDefault";
        final Consumer<EventDetails> handlerToRun = mockHandler();

        TestEventsProvider defaultProvider = new TestEventsProvider(INIT_DELAY);
        api.setProviderAndWait(defaultProvider);

        Client client = api.getClient(name);
        client.onProviderConfigurationChanged(handlerToRun);

        // await the new provider to make sure the old one is shut down
        await().until(() -> defaultProvider.getState().equals(ProviderState.READY));

        // fire event on default provider
        defaultProvider.mockEvent(
                ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                ProviderEventDetails.builder().build());

        verify(handlerToRun, timeout(TIMEOUT)).accept(any());
        api.setProviderAndWait(new NoOpProvider());
    }

    @Test
    @DisplayName("subsequent handlers run if earlier throws")
    @Specification(
            number = "5.2.5",
            text = "If a handler function terminates abnormally, other handler functions MUST run.")
    void handlersRunIfOneThrows() {
        final String name = "handlersRunIfOneThrows";
        final Consumer<EventDetails> errorHandler = mockHandler();
        doThrow(new NullPointerException()).when(errorHandler).accept(any());
        final Consumer<EventDetails> nextHandler = mockHandler();
        final Consumer<EventDetails> lastHandler = mockHandler();

        TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
        api.setProviderAndWait(name, provider);

        Client client1 = api.getClient(name);

        client1.onProviderConfigurationChanged(errorHandler);
        client1.onProviderConfigurationChanged(nextHandler);
        client1.onProviderConfigurationChanged(lastHandler);

        provider.mockEvent(
                ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                ProviderEventDetails.builder().build());
        verify(errorHandler, timeout(TIMEOUT)).accept(any());
        verify(nextHandler, timeout(TIMEOUT)).accept(any());
        verify(lastHandler, timeout(TIMEOUT)).accept(any());
    }

    @Test
    @DisplayName("should have all properties")
    @Specification(number = "5.2.4", text = "The handler function MUST accept a event details parameter.")
    @Specification(
            number = "5.2.3",
            text = "The `event details` MUST contain the `provider name` associated with the event.")
    void shouldHaveAllProperties() {
        final Consumer<EventDetails> handler1 = mockHandler();
        final Consumer<EventDetails> handler2 = mockHandler();
        final String name = "shouldHaveAllProperties";

        TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
        api.setProviderAndWait(name, provider);
        Client client = api.getClient(name);

        // attached handlers
        api.onProviderConfigurationChanged(handler1);
        client.onProviderConfigurationChanged(handler2);

        List<String> flagsChanged = Arrays.asList("flag");
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addInteger("int", 1).build();
        String message = "a message";
        ProviderEventDetails details = ProviderEventDetails.builder()
                .eventMetadata(metadata)
                .flagsChanged(flagsChanged)
                .message(message)
                .build();

        provider.mockEvent(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);

        // both global and client handler should have all the fields.
        verify(handler1, timeout(TIMEOUT)).accept(argThat((EventDetails eventDetails) -> {
            return metadata.equals(eventDetails.getEventMetadata())
                    // TODO: issue for client name in events
                    && flagsChanged.equals(eventDetails.getFlagsChanged())
                    && message.equals(eventDetails.getMessage());
        }));
        verify(handler2, timeout(TIMEOUT)).accept(argThat((EventDetails eventDetails) -> {
            return metadata.equals(eventDetails.getEventMetadata())
                    && flagsChanged.equals(eventDetails.getFlagsChanged())
                    && message.equals(eventDetails.getMessage())
                    && name.equals(eventDetails.getDomain());
        }));
    }

    @Test
    @DisplayName("if the provider is ready handlers must run immediately")
    @Specification(
            number = "5.3.3",
            text = "Handlers attached after the provider is already in the associated state, MUST run immediately.")
    void matchingReadyEventsMustRunImmediately() {
        final String name = "matchingReadyEventsMustRunImmediately";
        final Consumer<EventDetails> handler = mockHandler();

        // provider which is already ready
        TestEventsProvider provider = new TestEventsProvider();
        api.setProviderAndWait(name, provider);

        // should run even thought handler was added after ready
        Client client = api.getClient(name);
        client.onProviderReady(handler);
        verify(handler, timeout(TIMEOUT)).accept(any());
    }

    @Test
    @DisplayName("if the provider is ready handlers must run immediately")
    @Specification(
            number = "5.3.3",
            text = "Handlers attached after the provider is already in the associated state, MUST run immediately.")
    void matchingStaleEventsMustRunImmediately() {
        final String name = "matchingStaleEventsMustRunImmediately";
        final Consumer<EventDetails> handler = mockHandler();

        // provider which is already stale
        TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
        Client client = api.getClient(name);
        api.setProviderAndWait(name, provider);
        provider.emitProviderStale(ProviderEventDetails.builder().build());
        assertThat(client.getProviderState()).isEqualTo(ProviderState.STALE);

        // should run even though handler was added after stale
        client.onProviderStale(handler);
        verify(handler, timeout(TIMEOUT)).accept(any());
    }

    @Test
    @DisplayName("if the provider is ready handlers must run immediately")
    @Specification(
            number = "5.3.3",
            text = "Handlers attached after the provider is already in the associated state, MUST run immediately.")
    void matchingErrorEventsMustRunImmediately() {
        final String name = "matchingErrorEventsMustRunImmediately";
        final Consumer<EventDetails> handler = mockHandler();

        // provider which is already in error
        TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
        Client client = api.getClient(name);
        api.setProviderAndWait(name, provider);
        provider.emitProviderError(ProviderEventDetails.builder().build());
        assertThat(client.getProviderState()).isEqualTo(ProviderState.ERROR);

        verify(handler, never()).accept(any());
        // should run even though handler was added after error
        client.onProviderError(handler);
        verify(handler, timeout(TIMEOUT)).accept(any());
    }

    @Test
    @DisplayName("must persist across changes")
    @Specification(number = "5.2.6", text = "Event handlers MUST persist across provider changes.")
    void mustPersistAcrossChanges() {
        final String name = "mustPersistAcrossChanges";
        final Consumer<EventDetails> handler = mockHandler();

        TestEventsProvider provider1 = new TestEventsProvider(INIT_DELAY);
        TestEventsProvider provider2 = new TestEventsProvider(INIT_DELAY);

        api.setProviderAndWait(name, provider1);
        Client client = api.getClient(name);
        client.onProviderConfigurationChanged(handler);

        provider1.mockEvent(
                ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                ProviderEventDetails.builder().build());
        ArgumentMatcher<EventDetails> nameMatches =
                (EventDetails details) -> details.getDomain().equals(name);

        verify(handler, timeout(TIMEOUT).times(1)).accept(argThat(nameMatches));

        // wait for the new provider to be ready.
        api.setProviderAndWait(name, provider2);

        // verify that with the new provider under the same name, the handler is called
        // again.
        provider2.mockEvent(
                ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                ProviderEventDetails.builder().build());
        verify(handler, timeout(TIMEOUT).times(2)).accept(argThat(nameMatches));
    }

    @Nested
    class HandlerRemoval {
        @Specification(
                number = "5.2.7",
                text = "The API and client MUST provide a function allowing the removal of event handlers.")
        @Test
        @DisplayName("should not run removed events")
        @SneakyThrows
        void removedEventsShouldNotRun() {
            final String name = "removedEventsShouldNotRun";
            final Consumer<EventDetails> handler1 = mockHandler();
            final Consumer<EventDetails> handler2 = mockHandler();

            TestEventsProvider provider = new TestEventsProvider(INIT_DELAY);
            api.setProviderAndWait(name, provider);
            Client client = api.getClient(name);

            // attached handlers
            api.onProviderStale(handler1);
            client.onProviderConfigurationChanged(handler2);

            api.removeHandler(ProviderEvent.PROVIDER_STALE, handler1);
            client.removeHandler(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, handler2);

            // emit event
            provider.mockEvent(
                    ProviderEvent.PROVIDER_CONFIGURATION_CHANGED,
                    ProviderEventDetails.builder().build());

            // both global and client handlers should not run.
            verify(handler1, after(TIMEOUT).never()).accept(any());
            verify(handler2, never()).accept(any());
        }
    }

    @Specification(
            number = "5.1.4",
            text = "PROVIDER_ERROR events SHOULD populate the provider event details's error message field.")
    @Test
    void thisIsAProviderRequirement() {}

    @SuppressWarnings("unchecked")
    private static Consumer<EventDetails> mockHandler() {
        return mock(Consumer.class);
    }
}
