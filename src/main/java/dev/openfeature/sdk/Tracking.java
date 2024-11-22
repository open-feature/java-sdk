package dev.openfeature.sdk;

/**
 * Interface for Tracking events.
 */
public interface Tracking {
    /**
     * Performs tracking of a particular action or application state.
     *
     * @param trackingEventName Event name to track
     */
    void track(String trackingEventName);

    /**
     * Performs tracking of a particular action or application state.
     *
     * @param trackingEventName Event name to track
     * @param context           Evaluation context used in flag evaluation
     */
    void track(String trackingEventName, EvaluationContext context);

    /**
     * Performs tracking of a particular action or application state.
     *
     * @param trackingEventName Event name to track
     * @param details           Data pertinent to a particular tracking event
     */
    void track(String trackingEventName, TrackingEventDetails details);

    /**
     * Performs tracking of a particular action or application state.
     *
     * @param trackingEventName Event name to track
     * @param context           Evaluation context used in flag evaluation
     * @param details           Data pertinent to a particular tracking event
     */
    void track(String trackingEventName, EvaluationContext context, TrackingEventDetails details);
}
