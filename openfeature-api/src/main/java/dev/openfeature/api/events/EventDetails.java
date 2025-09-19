package dev.openfeature.api.events;

/**
 * Eventdetails with provider information.
 */
public interface EventDetails extends ProviderEventDetails {

    EventDetails EMPTY = of("", ProviderEventDetails.EMPTY);

    static EventDetails of(String name, String domain) {
        return of(name, domain, ProviderEventDetails.EMPTY);
    }

    static EventDetails of(String name, String domain, ProviderEventDetails details) {
        return new DefaultEventDetails(name, domain, details);
    }

    static EventDetails of(String name, ProviderEventDetails details) {
        return of(name, null, details);
    }

    String getProviderName();

    String getDomain();
}
