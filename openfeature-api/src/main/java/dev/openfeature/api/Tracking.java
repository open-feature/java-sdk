package dev.openfeature.api;

/**
 * Interface for Tracking events.
 */
public interface Tracking {
    /**
     * Performs tracking of a particular action or application state.
     *
     * @param trackingEventName Event name to track
     * @throws IllegalArgumentException if {@code trackingEventName} is null
     */
    void track(String trackingEventName);

    /**
     * Performs tracking of a particular action or application state.
     *
     * @param trackingEventName Event name to track
     * @param context           Evaluation context used in flag evaluation
     * @throws IllegalArgumentException if {@code trackingEventName} is null
     */
    void track(String trackingEventName, EvaluationContext context);

    /**
     * Performs tracking of a particular action or application state.
     *
     * @param trackingEventName Event name to track
     * @param details           Data pertinent to a particular tracking event
     * @throws IllegalArgumentException if {@code trackingEventName} is null
     */
    void track(String trackingEventName, TrackingEventDetails details);

    /**
     * Performs tracking of a particular action or application state.
     *
     * @param trackingEventName Event name to track
     * @param context           Evaluation context used in flag evaluation
     * @param details           Data pertinent to a particular tracking event
     * @throws IllegalArgumentException if {@code trackingEventName} is null
     */
    void track(String trackingEventName, EvaluationContext context, TrackingEventDetails details);
}
